package com.gmc.trading.infra.support.api.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GapHighlowResponse {

  private List<BigDecimal> data;
}
