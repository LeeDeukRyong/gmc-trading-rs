package com.gmc.trading.modules.coin.application.dto;

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
public class OperatorBotCoinSearchRequest extends SearchRequestDto {

  private Long exchangeId;
  private Long marketId;
}
