package com.gmc.trading.modules.exchange.application.dto;

import java.math.BigDecimal;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeGapUpdate {

  @NotEmpty
  @Valid
  private Set<AddOrderGap> addOrderGaps;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class AddOrderGap {

    @NotNull
    private Integer addOrderCnt;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal gap;
  }
}