package com.gmc.trading.modules.volume_bot.domain;

import com.gmc.common.code.converter.trading.TradingStatusConverter;
import com.gmc.common.code.trading.TradingStatus;
import com.gmc.common.embedded.Created;
import com.gmc.common.exception.BizException;
import com.gmc.common.utils.CustomWebUtils;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeOrderResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class VolumeBotOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "volumeBotId", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk01_tt_volume_bot_order"), nullable = false)
  private VolumeBot volumeBot;

  @Convert(converter = TradingStatusConverter.class)
  @Column(length = 3)
  private TradingStatus buyTradingStatus;

  private String buyOrderId;

  @Builder.Default
  @Column(nullable = false)
  private BigDecimal buyOrderQty = BigDecimal.ZERO;

  @Builder.Default
  @Column(nullable = false)
  private BigDecimal buyTradingQty = BigDecimal.ZERO;

  @Builder.Default
  @Column(nullable = false)
  private BigDecimal buyCancelQty = BigDecimal.ZERO;

  @Builder.Default
  @Column(nullable = false)
  private BigDecimal buyPrice = BigDecimal.ZERO;

  @Builder.Default
  @Column(nullable = false)
  private BigDecimal buyFee = BigDecimal.ZERO;

  @Builder.Default
  @Column(nullable = false)
  private BigDecimal buyFeeQty = BigDecimal.ZERO;

  private LocalDateTime buyTradingDt;

  @Convert(converter = TradingStatusConverter.class)
  @Column(length = 3)
  private TradingStatus sellTradingStatus;

  private String sellOrderId;

  @Builder.Default
  @Column(nullable = false)
  private BigDecimal sellOrderQty = BigDecimal.ZERO;

  @Builder.Default
  @Column(nullable = false)
  private BigDecimal sellTradingQty = BigDecimal.ZERO;

  @Builder.Default
  @Column(nullable = false)
  private BigDecimal sellCancelQty = BigDecimal.ZERO;

  @Builder.Default
  @Column(nullable = false)
  private BigDecimal sellPrice = BigDecimal.ZERO;

  @Builder.Default
  @Column(nullable = false)
  private BigDecimal sellFee = BigDecimal.ZERO;

  @Builder.Default
  @Column(nullable = false)
  private BigDecimal sellFeeQty = BigDecimal.ZERO;

  private LocalDateTime sellTradingDt;

  /* =================================================================
   * Default columns
   ================================================================= */
  @Embedded
  @Builder.Default
  private final Created created = new Created();

  /* =================================================================
   * Domain mapping
   ================================================================= */

  /* =================================================================
   * Relation method
   ================================================================= */
  public void setVolumeBot(@NonNull VolumeBot volumeBot) {
    this.volumeBot = volumeBot;
  }

  public BigDecimal getBuyTradingQty() {
    return buyTradingQty;
  }

  public BigDecimal getSellAmount() {
    return sellTradingQty.multiply(sellPrice);
  }

  public BigDecimal getSellTradingQty() {
    return sellTradingQty;
  }

  public BigDecimal getBuyAmount() {
    return buyTradingQty.multiply(buyPrice);
  }

  public boolean isTodayTrading() {
    TimeZone clientTimeZone = CustomWebUtils.getClientTimezone();
    return created.getCreatedOn().atZone(ZoneId.systemDefault()).withZoneSameInstant(clientTimeZone.toZoneId()).toLocalDate()
        .isEqual(LocalDate.now(clientTimeZone.toZoneId()));
  }

  public boolean isCompleteTrading() {
    return isBuyCompleteTrading() && isSellCompleteTrading();
  }

  public boolean isNotCompleteTrading() {
    return !isCompleteTrading();
  }

  public boolean isBuyCompleteTrading() {
    return buyTradingStatus == null || TradingStatus.COMPLETE_TRADING.equals(buyTradingStatus) || isBuyManualCancelTrading();
  }

  public boolean isBuyNotCompleteTrading() {
    return !isBuyCompleteTrading();
  }

  private boolean isBuyManualCancelTrading() {
    return TradingStatus.MANUAL_CANCEL.equals(buyTradingStatus);
  }

  public boolean isSellCompleteTrading() {
    return sellTradingStatus == null || TradingStatus.COMPLETE_TRADING.equals(sellTradingStatus) || isSellManualCancelTrading();
  }

  public boolean isSellNotCompleteTrading() {
    return !isSellCompleteTrading();
  }

  private boolean isSellManualCancelTrading() {
    return TradingStatus.MANUAL_CANCEL.equals(sellTradingStatus);
  }

  /* =================================================================
   * Business logic
   ================================================================= */
  public void setBuyInfo(ExchangeOrderResponse buyInfo) {
    buyTradingStatus = buyInfo.getTradingStatus();
    buyOrderId = buyInfo.getOrderId();
    buyPrice = buyInfo.getPrice();
    buyOrderQty = buyInfo.getOrderQty();
    buyTradingQty = buyInfo.getTradingQty();
    if (buyInfo.isComplete()) {
      buyCancelQty = buyOrderQty.subtract(buyTradingQty);
    }
    buyFee = buyInfo.getFee();
    buyFeeQty = buyInfo.getFeeQty();
    buyTradingDt = buyInfo.getTradingDt() != null ? buyInfo.getTradingDt() : buyInfo.getOrderDt();
  }

  public void setSellInfo(ExchangeOrderResponse sellInfo) {
    sellTradingStatus = sellInfo.getTradingStatus();
    sellOrderId = sellInfo.getOrderId();
    sellPrice = sellInfo.getPrice();
    sellOrderQty = sellInfo.getOrderQty();
    sellTradingQty = sellInfo.getTradingQty();
    if (sellInfo.isComplete()) {
      sellCancelQty = sellOrderQty.subtract(sellTradingQty);
    }
    sellFee = sellInfo.getFee();
    sellFeeQty = sellInfo.getFeeQty();
    sellTradingDt = sellInfo.getTradingDt() != null ? sellInfo.getTradingDt() : sellInfo.getOrderDt();
  }

  /**
   * 매수, 매도 주문 체크 및 결과 업데이트
   *
   * @throws BizException 주문 정보 확인 할 수 없는 경우
   */
  public void orderCheck() throws BizException {
    if (StringUtils.isNotBlank(buyOrderId)) {
      setBuyInfo(volumeBot.getExchangeHandler().getOrder(volumeBot.getExchangeApiKey(), volumeBot.getCoin(), buyOrderId));
    }

    if (StringUtils.isNotBlank(sellOrderId)) {
      setSellInfo(volumeBot.getExchangeHandler().getOrder(volumeBot.getExchangeApiKey(), volumeBot.getCoin(), sellOrderId));
    }
  }

  /**
   * 미체결 주문 취소 및 결과 업데이트
   *
   * @throws BizException 주문 취소 실패인 경우
   */
  public void orderCancel() throws BizException {
    if (StringUtils.isNotBlank(buyOrderId) && isBuyNotCompleteTrading()) {
      setBuyInfo(volumeBot.getExchangeHandler().orderCancel(volumeBot.getExchangeApiKey(), volumeBot.getCoin(), buyOrderId));
    }

    if (StringUtils.isNotBlank(sellOrderId) && isSellNotCompleteTrading()) {
      setSellInfo(volumeBot.getExchangeHandler().orderCancel(volumeBot.getExchangeApiKey(), volumeBot.getCoin(), sellOrderId));
    }
  }

  public boolean isNotCrossTrading() {
    return isCompleteTrading() && buyOrderQty.equals(buyCancelQty) && sellOrderQty.equals(sellCancelQty);
  }

  public boolean hasSave() {
    return StringUtils.isNotBlank(buyOrderId) || StringUtils.isNotBlank(sellOrderId);
  }
}