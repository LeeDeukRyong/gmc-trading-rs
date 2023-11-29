package com.gmc.trading.modules.exchange.domain;

import static java.util.stream.Collectors.toSet;

import com.gmc.common.code.common.CurrencyCode;
import com.gmc.common.code.common.IsYn;
import com.gmc.common.code.converter.common.CurrencyCodeConverter;
import com.gmc.common.code.converter.trading.ExchangeCodeConverter;
import com.gmc.common.code.trading.ExchangeCode;
import com.gmc.common.embedded.CreatedAndUpdated;
import com.gmc.common.exception.BizException;
import com.gmc.common.support.api.AdminApi;
import com.gmc.common.utils.ApplicationContextUtils;
import com.gmc.trading.infra.support.handler.HandlerFactory;
import com.gmc.trading.infra.support.handler.exchange.ExchangeHandler;
import com.gmc.trading.modules.api_key.domain.ApiKey;
import com.gmc.trading.modules.bot.domain.Bot;
import com.gmc.trading.modules.coin.domain.Coin;
import com.gmc.trading.modules.exchange.application.dto.ExchangeGapUpdate;
import com.gmc.trading.modules.exchange.application.dto.ExchangeGapUpdate.AddOrderGap;
import com.gmc.trading.modules.exchange.application.dto.ExchangeResponse;
import com.gmc.trading.modules.exchange.application.dto.ExchangeUpdate;
import com.gmc.trading.modules.market.domain.Market;
import com.gmc.trading.modules.wallet.domain.Wallet;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "uk01_tt_exchange", columnNames = {"code"})})
public class Exchange {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Convert(converter = ExchangeCodeConverter.class)
  @Column(length = 3, nullable = false, updatable = false)
  private ExchangeCode code;

  @Convert(converter = CurrencyCodeConverter.class)
  @Column(length = 4, nullable = false)
  private CurrencyCode currencyCode;

  @Column(precision = 14, scale = 2, nullable = false)
  private BigDecimal minFunds;

  @Column(precision = 14, scale = 2, nullable = false)
  private BigDecimal minOrderPrice;

  @Column(precision = 5, scale = 4, nullable = false)
  private BigDecimal fee;

  @Builder.Default
  @Column(nullable = false)
  private Integer apiCnt = 1;

  @Builder.Default
  @Column(nullable = false)
  private Integer botCnt = 1;

  @Column(nullable = false)
  private BigDecimal defaultProfitGap;

  @Builder.Default
  @Column(length = 1, nullable = false)
  @Enumerated(EnumType.STRING)
  private IsYn useYn = IsYn.Y;

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
  @OneToMany(mappedBy = "exchange", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private final Set<Market> markets = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "exchange", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private final Set<ApiKey> apiKeys = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "exchange", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private final Set<ExchangeOperator> operators = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "exchange", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private final Set<ExchangeGap> gaps = new HashSet<>();

  /* =================================================================
   * Relation method
   ================================================================= */
  public void addOperator(long operatorId) {
    if (operators.stream().noneMatch(f -> f.getOperatorId().equals(operatorId))) {
      if (IsYn.N.equals(ApplicationContextUtils.getBean(AdminApi.class).getOperator(operatorId).getResponse().getUseYn())) {
        throw new BizException("사용할 수 없는 운영사 입니다.");
      }

      operators.add(ExchangeOperator.builder().exchange(this).operatorId(operatorId).build());
    }
  }

  public void addOperators(List<Long> operatorIds) {
    operatorIds.forEach(this::addOperator);
  }

  public void removeOperator(long operatorId) {
    operators.removeIf(f -> f.getOperatorId().equals(operatorId));
  }

  public void removeOperators(List<Long> operatorIds) {
    operatorIds.forEach(this::removeOperator);
  }

  /**
   * <pre>
   * ex) 추가 주문 횟수 X 일 때 tt_exchange_gap.add_order_cnt 값이 X 인 데이터 찾아서 해당 데이터의 gap 정보 리턴
   *     만약에 추가 주문 횟수 X 값이 없는 경우 defaultProfitGap + (추가 주문횟수 * 0.1) 리턴
   * </pre>
   *
   * @param addOrderCnt 추가 주문 횟수
   * @return 추가 주문 횟수에 따른 간격 값
   */
  public BigDecimal getAddOrderGap(int addOrderCnt) {
    return gaps.stream().filter(f -> f.getAddOrderCnt().equals(addOrderCnt)).map(ExchangeGap::getGap).findFirst()
        .orElse(defaultProfitGap.add(BigDecimal.valueOf(addOrderCnt).multiply(BigDecimal.valueOf(0.1))));
  }

  public ExchangeHandler getExchangeHandler() throws BizException {
    return HandlerFactory.getExchangeHandler(code);
  }

  public Set<Coin> getCoins() {
    return markets.stream().flatMap(m -> m.getCoins().stream()).collect(toSet());
  }

  public Set<Wallet> getWallets() {
    return apiKeys.stream().flatMap(f -> f.getWallets().stream()).collect(toSet());
  }

  public Wallet getMaintenanceWallet(@NonNull String coinCode) {
    return getWallets().stream().filter(f -> f.isMaintenanceWallet() && f.getCoinCode().equals(coinCode)).findAny()
        .orElseThrow(() -> new BizException("등록된 유지비 지값이 없습니다."));
  }

  public boolean hasMaintenanceWallet(@NonNull String coinCode) {
    return getWallets().stream().anyMatch(f -> f.isMaintenanceWallet() && f.getCoinCode().equals(coinCode));
  }

  public Wallet getDefaultPaymentWallet(long operatorId) {
    return getWallets().stream().filter(f -> f.getAccount().getOperatorId().equals(operatorId) && f.isDefaultPaymentWallet()).findAny()
        .orElseThrow(() -> new BizException("등록된 기본 결제 지값이 없습니다."));
  }

  public boolean hasDefaultPaymentWallet(long operatorId) {
    return getWallets().stream().anyMatch(f -> f.getAccount().getOperatorId().equals(operatorId) && f.isDefaultPaymentWallet());
  }

  public boolean isUsable() {
    return IsYn.Y.equals(useYn);
  }

  /**
   * @param operatorId 운영사 아이디
   * @throws BizException 사용할 수 없는 거래소 인 경우
   */
  public void validExchangeUsable(long operatorId) throws BizException {
    if (!isUsable() || operators.stream().noneMatch(f -> f.getOperatorId().equals(operatorId))) {
      throw new BizException("사용할 수 없는 거래소 입니다.");
    }
  }

  /**
   * @param price 금액
   * @return 수수료 제외한 금액
   */
  public BigDecimal withOutFee(BigDecimal price) {
    // 수수료 계산 (1 - 거래소 수수료 / 100) * 금액
    return BigDecimal.ONE.subtract(fee.multiply(BigDecimal.valueOf(0.01))).multiply(price);
  }

  public ExchangeResponse ofResponse() {
    ExchangeResponse response = ExchangeResponse.builder().id(id).code(code).currencyCode(currencyCode).minFunds(minFunds)
        .minOrderPrice(minOrderPrice).fee(fee).apiCnt(apiCnt).botCnt(botCnt).defaultProfitGap(defaultProfitGap).useYn(useYn).build();
    response.setCreatedAndUpdated(createdAndUpdated);

    return response;
  }

  public ExchangeResponse ofDetailResponse() {
    ExchangeResponse response = ofResponse();
    response.setGaps(gaps.stream().sorted(Comparator.comparing(ExchangeGap::getAddOrderCnt)).map(ExchangeGap::ofResponse).toList());
    response.setMarkets(markets.stream().map(Market::ofResponse).toList());
    response.setOperators(
        operators.stream().sorted(Comparator.comparing(ExchangeOperator::getOperatorId)).map(ExchangeOperator::getOperatorId).toList());

    return response;
  }

  /* =================================================================
   * Business logic
   ================================================================= */
  public void update(@NonNull ExchangeUpdate update) {
    if (update.getCurrencyCode() != null) {
      currencyCode = update.getCurrencyCode();
    }

    if (update.getMinFunds() != null) {
      minFunds = update.getMinFunds();
    }

    if (update.getMinOrderPrice() != null) {
      minOrderPrice = update.getMinOrderPrice();
    }

    if (update.getFee() != null) {
      fee = update.getFee();
    }

    if (update.getApiCnt() != null) {
      apiCnt = update.getApiCnt();
    }

    if (update.getBotCnt() != null) {
      botCnt = update.getBotCnt();
    }

    if (update.getDefaultProfitGap() != null) {
      defaultProfitGap = update.getDefaultProfitGap();
    }

    if (update.getUseYn() != null) {
      if (IsYn.N.equals(update.getUseYn())) {
        if (apiKeys.stream().anyMatch(a -> a.getBots().stream().anyMatch(Bot::isStarted))) {
          throw new BizException("거래소에 작동중인 봇이 있습니다.");
        }
      }

      useYn = update.getUseYn();
    }
  }

  public void updateGap(@NonNull ExchangeGapUpdate update) {
    for (AddOrderGap addOrderGap : update.getAddOrderGaps()) {
      gaps.stream().filter(f -> f.getAddOrderCnt().equals(addOrderGap.getAddOrderCnt())).findFirst()
          .ifPresentOrElse(gap -> gap.update(addOrderGap.getGap()),
              () -> gaps.add(ExchangeGap.builder().exchange(this).addOrderCnt(addOrderGap.getAddOrderCnt()).gap(addOrderGap.getGap()).build()));
    }
  }
}