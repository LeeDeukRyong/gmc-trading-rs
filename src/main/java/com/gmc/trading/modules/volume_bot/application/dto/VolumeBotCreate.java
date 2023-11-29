package com.gmc.trading.modules.volume_bot.application.dto;

import com.gmc.trading.modules.common.dto.ScutumBotCreate;
import com.gmc.trading.modules.volume_bot.domain.VolumeBot;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class VolumeBotCreate extends ScutumBotCreate {

  public VolumeBot ofEntity() {
    return VolumeBot.builder().minCycleSecond(getMinCycleSecond()).maxCycleSecond(getMaxCycleSecond()).minOrderAmount(getMinOrderAmount())
        .maxOrderAmount(getMaxOrderAmount()).build();
  }
}
