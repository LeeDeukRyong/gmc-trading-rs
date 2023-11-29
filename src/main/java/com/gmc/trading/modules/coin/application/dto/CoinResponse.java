package com.gmc.trading.modules.coin.application.dto;

import com.gmc.common.code.common.IsYn;
import com.gmc.common.dto.common.BaseResponse;
import com.gmc.trading.modules.exchange.application.dto.ExchangeResponse;
import com.gmc.trading.modules.market.application.dto.MarketResponse;
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
public class CoinResponse extends BaseResponse {

  private Long id;
  private String code;
  private String coinNm;
  private String coinNmEn;
  private IsYn warningYn;
  private BigDecimal tickSz;
  private BigDecimal lotSz;
  private BigDecimal maxLmtSz;
  private BigDecimal maxMktSz;
  private BigDecimal minSz;
  private BigDecimal minPrice;
  private BigDecimal maxPrice;
  private BigDecimal ctVal;
  private Integer lever;
  private IsYn useYn;
  private String remark;

  private BigDecimal currentPrice;
  private BigDecimal minimumFunds;
  private BigDecimal minOrderPrice;

  private MarketResponse market;
  private ExchangeResponse exchange;
}
