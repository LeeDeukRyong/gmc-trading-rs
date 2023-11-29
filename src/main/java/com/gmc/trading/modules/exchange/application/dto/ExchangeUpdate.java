package com.gmc.trading.modules.exchange.application.dto;

import com.gmc.common.code.common.CurrencyCode;
import com.gmc.common.code.common.IsYn;
import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeUpdate {

  private CurrencyCode currencyCode;
  @DecimalMin(value = "0.0", inclusive = false)
  @Digits(integer = 12, fraction = 2)
  private BigDecimal minFunds;
  @Digits(integer = 12, fraction = 2)
  private BigDecimal minOrderPrice;
  @DecimalMin(value = "0.0", inclusive = false)
  @Digits(integer = 1, fraction = 4)
  private BigDecimal fee;
  private Integer apiCnt;
  private Integer botCnt;
  @DecimalMin(value = "0.0", inclusive = false)
  @Digits(integer = 5, fraction = 2)
  private BigDecimal defaultProfitGap;
  private IsYn useYn;
}
