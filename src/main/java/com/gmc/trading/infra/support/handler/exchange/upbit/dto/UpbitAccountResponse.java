package com.gmc.trading.infra.support.handler.exchange.upbit.dto;

import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeCoinBalanceResponse;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpbitAccountResponse {

  private String currency; // 화폐를 의미하는 영문 대문자 코드
  private BigDecimal balance; // 주문가능 금액/수량
  private BigDecimal locked; // 주문 중 묶여있는 금액/수량
  private BigDecimal avg_buy_price; // 매수평균가
  private Boolean avg_buy_price_modified; // 매수평균가 수정 여부
  private String unit_currency; // 평단가 기준 화폐

  public ExchangeCoinBalanceResponse ofResponse() {
    return ExchangeCoinBalanceResponse.builder().currency(currency).balance(balance).avgBuyPrice(avg_buy_price).build();
  }
}
