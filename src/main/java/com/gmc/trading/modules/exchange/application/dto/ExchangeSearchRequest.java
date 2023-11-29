package com.gmc.trading.modules.exchange.application.dto;

import com.gmc.common.code.common.CurrencyCode;
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
public class ExchangeSearchRequest extends SearchRequestDto {

  private CurrencyCode currencyCode;
  private Integer apiCnt;
  private Integer botCnt;
}
