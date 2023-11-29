package com.gmc.trading.modules.common.dto;

import com.gmc.common.dto.common.CreateDtoWithUserId;
import com.gmc.trading.modules.common.validation.ValidScutumBotCreate;
import java.math.BigDecimal;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@ValidScutumBotCreate
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ScutumBotCreate extends CreateDtoWithUserId {

  @NotNull
  private Long apiKeyId;

  @NotNull
  private Long coinId;

  @Min(value = 3)
  @NotNull
  private Integer minCycleSecond;

  @Max(value = 86400)
  @NotNull
  private Integer maxCycleSecond;

  @NotNull
  private BigDecimal minOrderAmount;

  @NotNull
  private BigDecimal maxOrderAmount;
}