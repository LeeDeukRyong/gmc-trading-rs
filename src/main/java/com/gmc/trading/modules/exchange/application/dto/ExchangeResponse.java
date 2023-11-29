package com.gmc.trading.modules.exchange.application.dto;

import com.gmc.common.code.common.CurrencyCode;
import com.gmc.common.code.common.IsYn;
import com.gmc.common.code.trading.ExchangeCode;
import com.gmc.common.dto.common.BaseResponse;
import com.gmc.trading.modules.market.application.dto.MarketResponse;
import java.math.BigDecimal;
import java.util.List;
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
public class ExchangeResponse extends BaseResponse {

  private Long id;
  private ExchangeCode code;
  private CurrencyCode currencyCode;
  private BigDecimal minFunds;
  private BigDecimal minOrderPrice;
  private BigDecimal fee;
  private Integer apiCnt;
  private Integer botCnt;
  private BigDecimal defaultProfitGap;
  private IsYn useYn;

  private List<ExchangeGapResponse> gaps;

  private List<MarketResponse> markets;

  private List<Long> operators;
}
