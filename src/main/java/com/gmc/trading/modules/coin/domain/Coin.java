package com.gmc.trading.modules.coin.domain;

import com.gmc.common.code.common.IsYn;
import com.gmc.common.embedded.CreatedAndUpdated;
import com.gmc.common.exception.BizException;
import com.gmc.common.redis.dto.RedisCoinPrice;
import com.gmc.common.redis.service.RedisCoinService;
import com.gmc.common.utils.ApplicationContextUtils;
import com.gmc.trading.modules.bot.domain.Bot;
import com.gmc.trading.modules.coin.application.dto.CoinResponse;
import com.gmc.trading.modules.coin.application.dto.CoinUpdate;
import com.gmc.trading.modules.exchange.domain.Exchange;
import com.gmc.trading.modules.market.domain.Market;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Table(uniqueConstraints = {@UniqueConstraint(name = "uk01_tt_coin", columnNames = {"marketId", "code"})})
public class Coin {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "marketId", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk01_tt_coin"), nullable = false)
  private Market market;

  @Column(length = 30, nullable = false, updatable = false)
  private String code;

  @Column(length = 30, nullable = false)
  private String coinNm;

  @Column(length = 30)
  private String coinNmEn;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(length = 1, nullable = false)
  private IsYn warningYn = IsYn.N;

  @Column
  private BigDecimal tickSz;

  @Column
  private BigDecimal lotSz;

  @Column
  private BigDecimal maxLmtSz;

  @Column
  private BigDecimal maxMktSz;

  @Column
  private BigDecimal minSz;

  @Column
  private BigDecimal minPrice;

  @Column
  BigDecimal maxPrice;

  @Column
  private BigDecimal ctVal;

  @Column
  private Integer lever;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(length = 1, nullable = false)
  private IsYn useYn = IsYn.Y;

  @Column(length = 500)
  private String remark;

  /* =================================================================
   * Default columns
   ================================================================= */
  @Embedded
  @Builder.Default
  private final CreatedAndUpdated createdAndUpdated = new CreatedAndUpdated();

  /* =================================================================
   * Domain mapping
   ================================================================= */
  @Builder.Default
  @OneToMany(mappedBy = "coin", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private final Set<Bot> bots = new HashSet<>();

  /* =================================================================
   * Relation method
   ================================================================= */
  public void setMarket(@NonNull Market market) {
    this.market = market;
  }

  public Exchange getExchange() {
    return market.getExchange();
  }

  public BigDecimal getMinOrderPrice() {
    if (minPrice != null) {
      return minPrice;
    } else {
      if (minSz != null) {
        if (getMarket().isSpot()) {
          return getCurrentPrice().multiply(minSz);
        } else {
          if (getMarket().isCoinRevenue()) {
            // 코인으로 투자하는 마켓 = 최소 주문 코인 수량
            return getContractValue().divide(getCurrentPrice(), 8, RoundingMode.UP);
          } else {
            // 돈으로 투자하는 마켓 = 최소 주문 금액
            return getContractValue().multiply(getCurrentPrice());
          }
        }
      } else {
        return getExchange().getMinOrderPrice();
      }
    }
  }

  /**
   * @return 선물 1 계약당 수량
   */
  public BigDecimal getContractValue() {
    return ctVal == null ? BigDecimal.ONE : ctVal;
  }

  /**
   * @return 코인의 현재 가격
   */
  public BigDecimal getCurrentPrice() {
    validCoin();

    RedisCoinService redisCoinService = ApplicationContextUtils.getBean(RedisCoinService.class);
    RedisCoinPrice redisCoinPrice = redisCoinService.getPrice(getExchange().getCode(), market.getMarketType(), market.getCode(), code);

    if (redisCoinPrice.getDateTime().plusMinutes(5).isBefore(LocalDateTime.now())) {
      BigDecimal coinPrice = null;
      int cnt = 1;
      while (coinPrice == null && cnt != 4) {
        try {
          coinPrice = getExchange().getExchangeHandler().getCoinPrice(this);
        } catch (Exception e) {
          log.error(e.getMessage(), e);
        }

        ++cnt;
      }

      if (coinPrice != null) {
        redisCoinService.save(RedisCoinPrice.builder().price(coinPrice).dateTime(LocalDateTime.now()).build(), getExchange().getCode(),
            market.getMarketType(), market.getCode(), code);
        return coinPrice;
      } else {
        throw new BizException("코인 현재가를 조회할 수 없습니다.");
      }
    } else {
      return redisCoinPrice.getPrice();
    }
  }

  public boolean isUsable() {
    return IsYn.Y.equals(useYn);
  }

  /**
   * @throws BizException 사용할 수 없는 코인 인 경우
   */
  public void validCoin() throws BizException {
    if (!isUsable()) {
      throw new BizException("사용할 수 없는 코인 입니다.");
    }
  }

  public BigDecimal getMinimumFunds() {
    if (market.isSpot()) {
      return getExchange().getMinFunds();
    } else {
      return getMinOrderPrice().multiply(getMinimumOrderCntWeight());
    }
  }

  /**
   * <pre>
   *    최소주문에 따른 가중치: 1, 3, 7, 15, 31, 63, 127, 255, 511, 1023, 2047
   *
   *    주문횟수 1 = 1
   *    주문횟수 2 = 앞 결과 * 2 + 1 => 1 * 2 + 1 = 3
   *    주문횟수 3 = 3 * 2 + 1 = 7
   *    주문횟수 4 = 7 * 2 + 1 = 15
   *    주문횟수 5 = 15 * 2 + 1 = 31
   *    주문횟수 6 = 31 * 2 + 1 = 63
   *
   *    마지막 결과에 + 1(수수료)
   * </pre>
   *
   * @return 최소 주문에 따른 가중치 값
   */
  private BigDecimal getMinimumOrderCntWeight() {
    int weight = 1;

    for (int i = 2; i <= market.getMinOrderCnt(); i++) {
      weight = weight * 2 + 1;
    }

    return BigDecimal.valueOf(weight + 1);
  }

  public void validMinFunds(BigDecimal funds) throws BizException {
    if (funds.compareTo(getMinimumFunds()) < 0) {
      if (getMarket().isCoinRevenue()) {
        throw new BizException("운용수량이 최소 운용수량 보다 작습니다.");
      } else {
        throw new BizException("운용금액이 최소 운용금액 보다 작습니다.");
      }
    }
  }

  public CoinResponse ofResponseWithOutCoinPrice() {
    CoinResponse response = CoinResponse.builder().id(id).code(code).coinNm(coinNm).coinNmEn(coinNmEn).warningYn(warningYn).tickSz(tickSz)
        .lotSz(lotSz).maxLmtSz(maxLmtSz).maxMktSz(maxMktSz).minSz(minSz).minPrice(minPrice).maxPrice(maxPrice).ctVal(ctVal).lever(lever).useYn(useYn)
        .remark(remark).build();
    response.setCreatedAndUpdated(createdAndUpdated);
    response.setMinimumFunds(getMinimumFunds());

    if (isUsable()) {
      response.setMinOrderPrice(getMinOrderPrice());
    }

    return response;
  }

  public CoinResponse ofResponse() {
    BigDecimal currentPrice = null;
    try {
      if (isUsable()) {
        currentPrice = getCurrentPrice();
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    CoinResponse response = ofResponseWithOutCoinPrice();
    response.setCurrentPrice(currentPrice);

    return response;
  }

  public CoinResponse ofDetailResponse() {
    CoinResponse response = ofResponse();
    response.setMarket(market.ofResponse());
    response.setExchange(getExchange().ofResponse());

    return response;
  }

  /* =================================================================
   * Business logic
   ================================================================= */
  public void update(@NonNull Coin coin) {
    warningYn = coin.getWarningYn();
    tickSz = coin.getTickSz();
    lotSz = coin.getLotSz();
    maxLmtSz = coin.getMaxLmtSz();
    maxMktSz = coin.getMaxMktSz();
    minSz = coin.getMinSz();
    minPrice = coin.getMinPrice();
    maxPrice = coin.getMaxPrice();
    ctVal = coin.getCtVal();
    lever = coin.getLever();
    remark = coin.getRemark();
  }

  public void update(@NonNull CoinUpdate update) {
    if (StringUtils.isNotBlank(update.getCoinNm())) {
      coinNm = update.getCoinNm();
    }

    if (StringUtils.isNotBlank(update.getCoinNmEn())) {
      coinNmEn = update.getCoinNmEn();
    }

    if (update.getUseYn() != null) {
      if (IsYn.N.equals(update.getUseYn())) {
        if (bots.stream().anyMatch(Bot::isStarted)) {
          throw new BizException("코인을 사용하여 작동중인 봇이 있습니다.");
        }
      }

      useYn = update.getUseYn();
    }

    if (update.getWarningYn() != null) {
      warningYn = update.getWarningYn();
    }

    remark = update.getRemark();
  }

  public void delete() {
    useYn = IsYn.N;
  }
}
