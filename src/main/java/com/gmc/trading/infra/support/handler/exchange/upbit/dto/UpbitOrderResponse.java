package com.gmc.trading.infra.support.handler.exchange.upbit.dto;

import com.gmc.common.code.trading.OrderType;
import com.gmc.common.code.trading.TradingStatus;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeOrderResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpbitOrderResponse {

  private String uuid; // 주문의 고유 아이디
  private String side; // 주문 종류
  private String ord_type; // 주문 방식
  private BigDecimal price; // 주문 당시 화폐 가격 - 시장가 주문 시 주문한 총액, 지정가 주문 시 주문단가
  private String state; // 주문 상태 - wait, done, cancel 시장가 주문하면 cancel로만 리턴
  private String market; // 마켓의 유일키
  private String created_at; // 주문 생성 시간 - 주문하기(yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX), 주문조회에서 각각 다른 값으로 리턴함(yyyy-MM-dd'T'HH:mm:ssXXX)
  private BigDecimal volume; // 사용자가 입력한 주문 양 - 시장가 주문 시 해당 값 리턴 안함
  private BigDecimal remaining_volume; // 체결 후 남은 주문 양 - 시장가 주문 시 해당 값 리턴 안함
  private BigDecimal reserved_fee; // 수수료로 예약된 비용
  private BigDecimal remaining_fee; // 남은 수수료
  private BigDecimal paid_fee; // 사용된 수수료
  private BigDecimal locked; // 거래에 사용중인 비용
  private BigDecimal executed_volume; // 체결된 양
  private Integer trades_count; // 해당 주문에 걸린 체결 수
  private List<UpbitOrderTradeResponse> trades; // 체결 목록 - 개별주문 조회시 포함됨

  public boolean isLimit() {
    return "limit".equalsIgnoreCase(ord_type);
  }

  public boolean isComplete() {
    if (isLimit()) {
      return remaining_volume.signum() == 0 || !"wait".equalsIgnoreCase(state);
    } else {
      return CollectionUtils.isNotEmpty(trades);
    }
  }

  public ExchangeOrderResponse ofResponse() {
    // 주문수량
    BigDecimal orderQty;
    if (isLimit()) {
      orderQty = volume;
    } else {
      orderQty = executed_volume;
    }

    // 체결수량
    BigDecimal tradingQty = executed_volume;

    // 주문단가
    BigDecimal orderPrice;
    if (isLimit()) {
      if (CollectionUtils.isNotEmpty(trades)) {
        BigDecimal funds = trades.stream().map(UpbitOrderTradeResponse::getFunds).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal qty = trades.stream().map(UpbitOrderTradeResponse::getVolume).reduce(BigDecimal.ZERO, BigDecimal::add);
        orderPrice = funds.divide(qty, 4, RoundingMode.HALF_UP);
      } else {
        orderPrice = price;
      }
    } else {
      orderPrice = BigDecimal.ZERO;
      if (CollectionUtils.isNotEmpty(trades)) {
        orderPrice = trades.stream().map(UpbitOrderTradeResponse::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(trades.size()), 4, RoundingMode.HALF_UP);
      }
    }

    // 주문총액
    BigDecimal amount = tradingQty.multiply(orderPrice);

    // 평균단가
    BigDecimal avgPrice = BigDecimal.ZERO;
    if (amount.signum() != 0 && tradingQty.signum() != 0) {
      avgPrice = amount.divide(tradingQty, 4, RoundingMode.HALF_UP);
    }

    // 거래상태
    TradingStatus tradingStatus;
    if (isComplete()) {
      tradingStatus = orderQty.compareTo(executed_volume) == 0 ? TradingStatus.COMPLETE_TRADING : TradingStatus.MANUAL_CANCEL;
    } else {
      tradingStatus = executed_volume.signum() == 0 ? TradingStatus.NON_TRADING : TradingStatus.PARTIAL_TRADING;
    }

    // 주문일시
    LocalDateTime orderDt;
    try {
      orderDt = ZonedDateTime.parse(created_at, DateTimeFormatter.ISO_DATE_TIME).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    } catch (Exception e) {
      orderDt = LocalDateTime.now();
    }

    // 거래일시
    LocalDateTime tradingDt = null;
    if (CollectionUtils.isNotEmpty(trades)) {
      try {
        tradingDt = trades.stream().map(
            orderTrade -> ZonedDateTime.parse(orderTrade.getCreated_at(), DateTimeFormatter.ISO_DATE_TIME).withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime()).max(Comparator.naturalOrder()).orElse(null);
      } catch (Exception e) {
        tradingDt = LocalDateTime.now();
      }
    }

    return ExchangeOrderResponse.builder().orderId(uuid).orderType("ask".equalsIgnoreCase(side) ? OrderType.SELL : OrderType.BUY)
        .tradingStatus(tradingStatus).orderQty(orderQty).tradingQty(tradingQty).price(orderPrice).fee(paid_fee).feeQty(BigDecimal.ZERO).amount(amount)
        .avgPrice(avgPrice).orderDt(orderDt).tradingDt(tradingDt).isComplete(isComplete()).build();
  }
}
