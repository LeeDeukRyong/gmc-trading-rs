package com.gmc.trading.modules.volume_bot.application.dto;

import com.gmc.common.code.common.IsYn;
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
public class VolumeBotSearchRequest extends SearchRequestDto {

  private String email;
  private String userNm;
  private Long exchangeId;
  private Long marketId;
  private Long coinId;
  private IsYn workYn; // 작동여부
}