package com.gmc.trading.modules.exchange.application.dto;

import com.gmc.common.dto.common.BaseResponse;
import java.math.BigDecimal;
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
public class ExchangeGapResponse extends BaseResponse {

  private Integer addOrderCnt;
  private BigDecimal gap;
}