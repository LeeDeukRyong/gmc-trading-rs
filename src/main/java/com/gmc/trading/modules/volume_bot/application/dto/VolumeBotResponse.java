package com.gmc.trading.modules.volume_bot.application.dto;

import com.gmc.common.code.common.IsYn;
import com.gmc.common.dto.common.BaseResponse;
import com.gmc.trading.modules.account.application.dto.AccountResponse;
import com.gmc.trading.modules.api_key.application.dto.ApiKeyResponse;
import com.gmc.trading.modules.coin.application.dto.CoinResponse;
import com.gmc.trading.modules.common.dto.ScutumBotOrderStatistics;
import com.gmc.trading.modules.exchange.application.dto.ExchangeResponse;
import com.gmc.trading.modules.market.application.dto.MarketResponse;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class VolumeBotResponse extends BaseResponse {

  private Long id;
  private Integer minCycleSecond; // 최소 작동 주기(초)
  private Integer maxCycleSecond; // 최대 작동 주기(초)
  private BigDecimal minOrderAmount; // 최소 주문 금액
  private BigDecimal maxOrderAmount; // 최대 주문 금액
  private IsYn workYn; // 작동 여부
  private IsYn delYn; // 삭제 여부
  private String remark;

  private BigDecimal todaySellQty; // 오늘 하루 매도 수량
  private BigDecimal todaySellAvgPrice; // 오늘 하루 매도 평단가
  private BigDecimal todaySellAmount; // 오늘 하루 전체 매도 금액
  private BigDecimal sellQty; // 전체 매도 수량
  private BigDecimal sellAvgPrice; // 전체 매도 평단가
  private BigDecimal sellAmount; // 전체 매도 금액

  private BigDecimal todayBuyQty; // 오늘 하루 매수 수량
  private BigDecimal todayBuyAvgPrice; // 오늘 하루 매수 평단가
  private BigDecimal todayBuyAmount; // 오늘 하루 전체 매수 금액
  private BigDecimal buyQty; // 전체 매수 수량
  private BigDecimal buyAvgPrice; // 전체 매수 평단가
  private BigDecimal buyAmount; // 전체 매수 금액

  private AccountResponse account;
  private ExchangeResponse exchange;
  private MarketResponse market;
  private CoinResponse coin;
  private ApiKeyResponse apiKey;

  public void setStatistics(ScutumBotOrderStatistics statistics) {
    todaySellQty = statistics.getTodaySellQty();
    todaySellAvgPrice = statistics.getTodaySellAvgPrice();
    todaySellAmount = statistics.getTodaySellAmount();
    sellQty = statistics.getSellQty();
    sellAvgPrice = statistics.getSellAvgPrice();
    sellAmount = statistics.getSellAmount();
    todayBuyQty = statistics.getTodayBuyQty();
    todayBuyAvgPrice = statistics.getTodayBuyAvgPrice();
    todayBuyAmount = statistics.getTodayBuyAmount();
    buyQty = statistics.getBuyQty();
    buyAvgPrice = statistics.getBuyAvgPrice();
    buyAmount = statistics.getBuyAmount();
  }
}