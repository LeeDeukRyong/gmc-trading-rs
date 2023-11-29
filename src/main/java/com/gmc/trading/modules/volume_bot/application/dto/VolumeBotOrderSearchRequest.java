package com.gmc.trading.modules.volume_bot.application.dto;

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
public class VolumeBotOrderSearchRequest extends SearchRequestDto {

  private Long volumeBotId;
  private Long exchangeId;
  private Long marketId;
  private Long coinId;
}