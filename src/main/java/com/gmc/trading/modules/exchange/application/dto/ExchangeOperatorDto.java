package com.gmc.trading.modules.exchange.application.dto;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeOperatorDto {

  @NotEmpty
  private List<Long> operatorIds;
}