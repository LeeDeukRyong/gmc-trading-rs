package com.gmc.trading.infra.support.scheduler;

import com.gmc.common.code.common.IsYn;
import com.gmc.common.code.trading.ExchangeCode;
import com.gmc.common.code.trading.MarketType;
import com.gmc.common.dto.admin.operator.OperatorSearchRequest;
import com.gmc.common.dto.admin.operator.OperatorSearchResponse;
import com.gmc.common.redis.service.RedisCoinService;
import com.gmc.common.support.api.AdminApi;
import com.gmc.trading.infra.support.api.BinanceApi;
import com.gmc.trading.modules.settlement.application.SettlementService;
import com.gmc.trading.modules.settlement.application.dto.SettlementResponse;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

@Profile("!default")
@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementScheduler extends AbstractScheduler {

  private final BinanceApi binanceApi;
  private final RedisCoinService redisCoinService;
  private final AdminApi adminApi;
  private final SettlementService settlementService;

  @Scheduled(cron = "0 0 0 1 * *") // UTC 기준 매월 1일 0시에 동작
  @SchedulerLock(name = "scheduledSettlement", lockAtLeastFor = "PT23H", lockAtMostFor = "P1D")
  public void scheduledSettlement() {
    log.info("=============== Start Trading scheduledSettlement ===============");
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    List<CompletableFuture<SettlementResponse>> futures = new ArrayList<>();
    try {
      // 환율계산 기준 코인
      String exchangeCoinCode = "BTC";
      // 정산 년월
      String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
      // 기준 환율
      CompletableFuture<Integer> targetExchangeRate = CompletableFuture.supplyAsync(
          () -> redisCoinService.getPrice(ExchangeCode.UPBIT, MarketType.SPOT, "KRW", exchangeCoinCode).getPrice()
              .divide(binanceApi.getPriceUSDT(exchangeCoinCode), 0, RoundingMode.HALF_UP).intValue());

      // 정산 대상 운영사
      CompletableFuture<List<OperatorSearchResponse>> targetOperators = CompletableFuture.supplyAsync(
          () -> adminApi.getOperators(OperatorSearchRequest.builder().pointPaymentYn(IsYn.N).useYn(IsYn.Y).withOutPaging(true).build())
              .getResponse());

      // 환율, 정산 운영사 조회 후 정산
      futures = targetExchangeRate.thenCombine(targetOperators,
          (exchangeRate, operators) -> operators.stream().map(operator -> CompletableFuture.supplyAsync(() -> {
            setupToken();
            return settlementService.settlement(operator.getId(), date, operator.getMaintenanceFee(), exchangeRate);
          }, scheduler).exceptionally(e -> {
            log.error(e.getMessage(), e);
            return null;
          })).toList()).join();

      // 비동기로 모든 운영사 정산 완료 되면 로그 출력
      List<CompletableFuture<SettlementResponse>> finalFutures = futures;
      CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
          .thenApply(Void -> finalFutures.stream().map(CompletableFuture::join).toList())
          .thenAccept(result -> result.stream().filter(Objects::nonNull).map(SettlementResponse::toLogString).forEach(log::info))
          .join();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    } finally {
      stopWatch.stop();
      log.info("=== scheduledSettlement took: {} s, total: {}, success: {}", stopWatch.getTotalTimeSeconds(), futures.size(),
          futures.stream().filter(f -> f.join() != null).count());
      log.info("=============== Stop Trading scheduledSettlement ===============");
    }
  }
}
