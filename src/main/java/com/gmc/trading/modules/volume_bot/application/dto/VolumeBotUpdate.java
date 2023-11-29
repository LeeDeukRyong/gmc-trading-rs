package com.gmc.trading.modules.volume_bot.application.dto;

import com.gmc.common.code.common.IsYn;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolumeBotUpdate {

  @Min(value = 3)
  private Integer minCycleSecond;
  @Min(value = 3)
  private Integer maxCycleSecond;
  private BigDecimal minOrderAmount;
  private BigDecimal maxOrderAmount;
  private IsYn workYn;
}