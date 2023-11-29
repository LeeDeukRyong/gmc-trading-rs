package com.gmc.trading.modules.api_key.application.dto;

import com.gmc.trading.modules.exchange.application.dto.ExchangeResponse;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoneyPaybackQtyResponse {

  private Integer money;
  private String coinCode;
  private BigDecimal qty;
  private BigDecimal coinUsdtPrice;
  private BigDecimal spread;
  private String chain;
  private String address;
  private String extraAddress;

  private ExchangeResponse exchange;
}