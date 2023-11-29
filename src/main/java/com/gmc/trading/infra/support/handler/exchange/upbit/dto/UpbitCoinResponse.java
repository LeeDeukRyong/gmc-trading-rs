package com.gmc.trading.infra.support.handler.exchange.upbit.dto;

import com.gmc.common.code.common.IsYn;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeCoinResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpbitCoinResponse {

  private String market_warning;
  private String market;
  private String korean_name;
  private String english_name;

  public ExchangeCoinResponse ofResponse() {
    String[] split = market.split("-");
    return ExchangeCoinResponse.builder().marketCode(split[0]).coinCode(split[1]).coinNm(korean_name).coinNmEn(english_name)
        .warningYn(market_warning.equalsIgnoreCase("CAUTION") ? IsYn.Y : IsYn.N).lotSz("0.00000001").build();
  }
}
