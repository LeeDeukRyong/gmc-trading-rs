package com.gmc.trading.modules.account.domain;

import static java.util.stream.Collectors.toSet;

import com.gmc.common.exception.BizException;
import com.gmc.common.support.api.AdminApi;
import com.gmc.common.utils.ApplicationContextUtils;
import com.gmc.trading.modules.account.application.dto.AccountResponse;
import com.gmc.trading.modules.account.application.dto.ExchangeBotResponse;
import com.gmc.trading.modules.api_key.application.dto.ApiKeyResponse;
import com.gmc.trading.modules.api_key.domain.ApiKey;
import com.gmc.trading.modules.bot.domain.Bot;
import com.gmc.trading.modules.coin.application.dto.CoinResponse;
import com.gmc.trading.modules.coin.application.dto.CoinSearchRequest;
import com.gmc.trading.modules.coin.domain.Coin;
import com.gmc.trading.modules.exchange.domain.Exchange;
import com.gmc.trading.modules.payment.domain.Payment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Account {

  @Id
  private Long userId;

  @Column(length = 50, nullable = false)
  private String email;

  @Column(length = 30, nullable = false)
  private String userNm;

  @Column(nullable = false)
  private Long operatorId;

  @Column(nullable = false)
  private Long centerId;

  /* =================================================================
   * Domain mapping
   ================================================================= */
  @Builder.Default
  @OneToMany(mappedBy = "account", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private final Set<ApiKey> apiKeys = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "account", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private final Set<Bot> bots = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "account", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private final Set<Payment> payments = new HashSet<>();

  /* =================================================================
   * Relation method
   ================================================================= */
  public AccountResponse ofWitOutTradingBotResponse() {
    return AccountResponse.builder().userId(userId).email(email).userNm(userNm).operatorId(operatorId).centerId(centerId).build();
  }

  public AccountResponse ofResponse() {
    AccountResponse response = ofWitOutTradingBotResponse();
    response.setTotalBotCnt(getTotalBots().size());
    response.setStartBotCnt(getStartBots().size());
    response.setTotalFunds(getTotalFunds());

    return response;
  }

  public AccountResponse ofDetailResponse(@NonNull TimeZone timeZone) {
    AccountResponse response = ofResponse();
    response.setExchangeBots(
        getTotalBots().stream().map(Bot::getExchange).distinct().map(exchange -> ofExchangeBotResponse(exchange, timeZone)).toList());

    return response;
  }

  public ExchangeBotResponse ofExchangeBotResponse(@NonNull Exchange exchange, @NonNull TimeZone timeZone) {
    Set<Bot> bots = getTotalBots(exchange.getId());

    ExchangeBotResponse response = ExchangeBotResponse.builder().totalBotCnt(bots.size()).startBotCnt(getStartBots(exchange.getId()).size())
        .totalFunds(getCurrentWorkingBotTotalFunds(exchange.getId())).revenueOfMonth(getRevenueOfMonth(exchange.getId(), timeZone))
        .revenueOfToday(getRevenueOfToday(exchange.getId(), timeZone)).exchange(exchange.ofResponse()).build();

    response.setBots(bots.stream().map(Bot::ofResponse).toList());

    return response;
  }

  /* =================================================================
   * Business logic
   ================================================================= */
  public void updateCenter(long centerId) {
    this.centerId = centerId;
    operatorId = ApplicationContextUtils.getBean(AdminApi.class).getCenter(centerId).getResponse().getOperator().getId();
  }

  /**
   * @return 만료되지 않은 사용자 apiKey 목록
   */
  public List<ApiKey> getUsableApiKeys() {
    return apiKeys.stream().filter(ApiKey::isNotExpired).toList();
  }

  /**
   * @param apiKeyId apiKey 아이디
   * @return apiKeyId에 해당하는 apiKey
   */
  public ApiKey getApiKey(long apiKeyId) {
    return getUsableApiKeys().stream().filter(f -> f.getId().equals(apiKeyId)).findAny()
        .orElseThrow(() -> new BizException("등록된 API Key에서 해당 API Key를 찾을 수 없습니다."));
  }

  /**
   * @return 거래소별 만료되지 않은 API key 목록
   */
  public List<ApiKey> getApiKeys(long exchangeId) {
    return getUsableApiKeys().stream().filter(f -> f.getExchange().getId().equals(exchangeId)).toList();
  }

  /**
   * @param exchangeId 거래소 아이디
   * @return 거래소별 봇 생성 가능한 apiKey 목록
   */
  public List<ApiKeyResponse> getCreatableBotApiKeys(long exchangeId) {
    return getApiKeys(exchangeId).stream().filter(ApiKey::isCreatableBot).map(ApiKey::ofResponse).toList();
  }

  /**
   * 거래소에 api key 등록 가능한지 체크
   */
  public void validCreatableApiKey(@NonNull Exchange exchange) throws BizException {
    if (getApiKeys(exchange.getId()).size() >= exchange.getApiCnt()) {
      throw new BizException("거래소에 등록 가능한 api key 수량을 초과하였습니다.");
    }
  }

  /**
   * <pre>
   *   exchangeId == null 봇 목록
   *   exchangeId가 != null 해당 거래소 봇 목록
   * </pre>
   *
   * @param exchangeId 거래소 아이디 nullable
   * @return 봇 목록
   */
  public Set<Bot> getBots(Long exchangeId) {
    if (exchangeId != null) {
      return bots.stream().filter(f -> f.getExchange().getId().equals(exchangeId)).collect(toSet());
    } else {
      return bots;
    }
  }

  /**
   * @return 삭제되지 않은(작동중 or 종료) 봇 목록
   */
  public Set<Bot> getTotalBots() {
    return bots.stream().filter(Bot::isNotDeleted).collect(toSet());
  }

  /**
   * @return 거래소별 삭제되지 않은(작동중 or 종료) 봇 목록
   */
  public Set<Bot> getTotalBots(long exchangeId) {
    return getTotalBots().stream().filter(f -> f.getExchange().getId().equals(exchangeId)).collect(toSet());
  }

  /**
   * @return 작동중인 봇 목록
   */
  public Set<Bot> getStartBots() {
    return bots.stream().filter(Bot::isStarted).collect(toSet());
  }

  /**
   * @return 거래소별 작동중인 봇 목록
   */
  public Set<Bot> getStartBots(long exchangeId) {
    return getStartBots().stream().filter(f -> f.getExchange().getId().equals(exchangeId)).collect(toSet());
  }

  public BigDecimal getTotalFunds() {
    return getStartBots().stream().map(Bot::getFunds).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * @return 거래소별 작동중인 봇의 전체 운용금액
   */
  public BigDecimal getCurrentWorkingBotTotalFunds(long exchangeId) {
    return getStartBots(exchangeId).stream().map(Bot::getFunds).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * UTC 기준 데이터 저장되기 때문에 시간 계산시 클라이언트의 타임존 정보 받아서 계산
   *
   * @return 거래소별 이번달 수익 금액
   */
  public BigDecimal getRevenueOfMonth(long exchangeId, @NonNull TimeZone timeZone) {
    return getBots(exchangeId).stream().map(bot -> bot.getRevenueOfMonth(timeZone)).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * UTC 기준 데이터 저장되기 때문에 시간 계산시 클라이언트의 타임존 정보 받아서 계산
   *
   * @return 거래소별 오늘 수익 금액
   */
  public BigDecimal getRevenueOfToday(long exchangeId, @NonNull TimeZone timeZone) {
    return getBots(exchangeId).stream().map(bot -> bot.getRevenueOfToday(timeZone)).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * <pre>
   *   exchangeId == null 처음 봇으로 투자한 날짜
   *   exchangeId가 != null 해당 거래소 처음 봇으로 투자한 날짜
   * </pre>
   *
   * @param exchangeId 거래소 아이디 nullable
   * @return 거래소에 처음 봇으로 투자한 날짜 투자한 기록이 없으면 오늘 날짜
   */
  public LocalDate getFirstInvestDate(Long exchangeId) {
    return getBots(exchangeId).stream().filter(Bot::hasCycle).map(Bot::getFirstInvestDate).min(LocalDate::compareTo).orElseGet(LocalDate::now);
  }

  /**
   * 거래소에 투자한 코인 목록 조회
   *
   * <pre>
   * from, to 모두 있으면 주어진 기간 동안 거래된 코인 목록
   * from 만 있으면 이후 거래된 코인 목록
   * to 만 잇으면 이전 거래된 코인 목록
   * </pre>
   *
   * @param request 거래소아이디(exchangeId), 시작일자(fromDate), 종료일자(toDate), timeZone
   * @return 거래된 코인 목록
   */
  public List<CoinResponse> getTradingCoins(CoinSearchRequest request) {
    return getBots(request.getExchangeId()).stream()
        .filter(f -> request.getMarketId() != null ? f.hasCycle() && f.getMarket().getId().equals(request.getMarketId()) : f.hasCycle())
        .flatMap(bot -> bot.getTradingCoins(request).stream()).distinct().map(Coin::ofResponseWithOutCoinPrice)
        .sorted(Comparator.comparing(CoinResponse::getCode)).toList();
  }
}