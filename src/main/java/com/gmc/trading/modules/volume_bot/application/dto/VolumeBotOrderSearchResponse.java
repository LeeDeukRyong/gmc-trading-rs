package com.gmc.trading.modules.volume_bot.application.dto;

import com.gmc.common.code.trading.ExchangeCode;
import com.gmc.common.code.trading.MarketType;
import com.gmc.common.code.trading.TradingStatus;
import com.gmc.common.dto.common.BaseResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class VolumeBotOrderSearchResponse extends BaseResponse {

  private Long id;
  private Long volumeBotId;
  private TradingStatus buyTradingStatus;
  private String buyOrderId;
  private BigDecimal buyOrderQty;
  private BigDecimal buyTradingQty;
  private BigDecimal buyCancelQty;
  private BigDecimal buyPrice;
  private BigDecimal buyFee;
  private BigDecimal buyFeeQty;
  private LocalDateTime buyTradingDt;
  private TradingStatus sellTradingStatus;
  private String sellOrderId;
  private BigDecimal sellOrderQty;
  private BigDecimal sellTradingQty;
  private BigDecimal sellCancelQty;
  private BigDecimal sellPrice;
  private BigDecimal sellFee;
  private BigDecimal sellFeeQty;
  private LocalDateTime sellTradingDt;

  private Long operatorId;
  private Long centerId;

  private Long exchangeId;
  private ExchangeCode exchangeCode;

  private Long marketId;
  private MarketType marketType;
  private String marketCode;

  private Long coinId;
  private String coinCode;
}
