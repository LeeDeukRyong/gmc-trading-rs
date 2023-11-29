package com.gmc.trading.modules.common.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScutumBotOrderStatistics {

  private BigDecimal sellQty;
  private BigDecimal sellAvgPrice;
  private BigDecimal sellAmount;
  private BigDecimal todaySellQty;
  private BigDecimal todaySellAvgPrice;
  private BigDecimal todaySellAmount;
  private BigDecimal buyQty;
  private BigDecimal buyAvgPrice;
  private BigDecimal buyAmount;
  private BigDecimal todayBuyQty;
  private BigDecimal todayBuyAvgPrice;
  private BigDecimal todayBuyAmount;
}