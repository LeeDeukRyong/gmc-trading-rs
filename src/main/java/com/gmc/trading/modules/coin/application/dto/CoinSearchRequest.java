package com.gmc.trading.modules.coin.application.dto;

import com.gmc.common.code.trading.ExchangeCode;
import com.gmc.common.code.trading.MarketType;
import com.gmc.common.dto.common.SearchRequestDto;
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
public class CoinSearchRequest extends SearchRequestDto {

  private String code;
  private String coinNm;
  private String remark;

  private Long marketId;
  private MarketType marketType;
  private String marketCode;

  private Long exchangeId;
  private ExchangeCode exchangeCode;
}
