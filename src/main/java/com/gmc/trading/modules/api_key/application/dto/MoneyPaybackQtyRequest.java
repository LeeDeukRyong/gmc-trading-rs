package com.gmc.trading.modules.api_key.application.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoneyPaybackQtyRequest {

  @NotNull
  @Min(value = 100)
  private Integer money;

  @NotNull
  private String coinCode;
}