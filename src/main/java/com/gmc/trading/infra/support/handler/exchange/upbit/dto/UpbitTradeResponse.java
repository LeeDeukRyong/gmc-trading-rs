package com.gmc.trading.infra.support.handler.exchange.upbit.dto;

import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeTradeResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpbitTradeResponse {

  private String market;
  private String trade_date_utc;
  private String trade_time_utc;
  private Long timestamp;
  private BigDecimal trade_price;
  private BigDecimal trade_volume;

  public ExchangeTradeResponse ofResponse() {
    return ExchangeTradeResponse.builder().price(trade_price).qty(trade_volume)
        .tradingDt(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()).build();
  }
}
