package com.gmc.trading.modules.api_key.domain;

import com.gmc.common.code.trading.MarketType;
import com.gmc.common.code.trading.TradingStatus;
import com.gmc.common.embedded.CreatedAndUpdated;
import com.gmc.common.exception.BizException;
import com.gmc.common.utils.PasswordEncoderUtils;
import com.gmc.common.utils.SecurityUtils;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeApiKey;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeCoinBalanceResponse;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeOrderResponse;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeWalletResponse;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeWithdrawRequest;
import com.gmc.trading.modules.account.domain.Account;
import com.gmc.trading.modules.api_key.application.dto.ApiKeyResponse;
import com.gmc.trading.modules.api_key.application.dto.ApiKeyUpdate;
import com.gmc.trading.modules.arbitrage_bot.domain.ArbitrageBotApi;
import com.gmc.trading.modules.bot.domain.Bot;
import com.gmc.trading.modules.buy_bot.domain.BuyBot;
import com.gmc.trading.modules.coin.domain.Coin;
import com.gmc.trading.modules.exchange.domain.Exchange;
import com.gmc.trading.modules.grid_bot.domain.GridBot;
import com.gmc.trading.modules.list_bot.domain.ListBot;
import com.gmc.trading.modules.payment.domain.Payment;
import com.gmc.trading.modules.sell_bot.domain.SellBot;
import com.gmc.trading.modules.volume_bot.domain.VolumeBot;
import com.gmc.trading.modules.wallet.domain.Wallet;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "uk01_tt_api_key", columnNames = {"accessKey"}),
    @UniqueConstraint(name = "uk02_tt_api_key", columnNames = {"userId", "keyName"})})
public class ApiKey {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "exchangeId", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk01_tt_api_key"), nullable = false)
  private Exchange exchange;

  @ManyToOne
  @JoinColumn(name = "userId", referencedColumnName = "userId", foreignKey = @ForeignKey(name = "fk02_tt_api_key"), nullable = false)
  private Account account;

  @Column(length = 100, nullable = false)
  private String keyName;

  @Column(length = 100, nullable = false)
  private String accessKey;

  @Column(length = 172, nullable = false)
  private String secretKey;

  @Column(length = 172)
  private String passPhrase;

  @Column
  private LocalDateTime expireDt;

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
  @OneToMany(mappedBy = "apiKey", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  private final Set<Bot> bots = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "apiKey", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  private final Set<VolumeBot> volumeBots = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "apiKey", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  private final Set<GridBot> gridBots = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "apiKey", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  private final Set<ListBot> listBots = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "apiKey", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  private final Set<BuyBot> buyBots = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "apiKey", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  private final Set<SellBot> sellBots = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "apiKey", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  private final Set<ArbitrageBotApi> arbitrageBotApis = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "apiKey", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  private Set<Payment> payments = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "apiKey", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Wallet> wallets = new HashSet<>();

  /* =================================================================
   * Relation method
   ================================================================= */
  public void setExchangeAndAccount(@NonNull Exchange exchange, @NonNull Account account) {
    this.exchange = exchange;
    this.account = account;

    validExchangeUsable();
  }

  private void validExchangeUsable() {
    if (!SecurityUtils.hasRoleAdmin()) {
      exchange.validExchangeUsable(account.getOperatorId());
    }
  }

  /**
   * @throws BizException 사용할 수 없는 api key 인 경우
   */
  public void validApiKey() throws BizException {
    validExchangeUsable();

    if (isExpired()) {
      throw new BizException("유효기간이 경과된 API key 입니다.");
    }
  }

  public void checkCreatableBot() throws BizException {
    validApiKey();

    if (!isCreatableBot()) {
      throw new BizException("거래소 API key 로 사용가능한 봇 수량을 초과하였습니다.");
    }
  }

  public boolean isCreatableBot() {
    return bots.stream().filter(Bot::isNotDeleted).count() +
        volumeBots.stream().filter(VolumeBot::isNotDeleted).count() +
        gridBots.stream().filter(GridBot::isNotDeleted).count() +
        listBots.stream().filter(ListBot::isNotDeleted).count() +
        buyBots.stream().filter(BuyBot::isNotDeleted).count() +
        sellBots.stream().filter(SellBot::isNotDeleted).count() +
        arbitrageBotApis.stream().filter(f -> f.getArbitrageBot().isNotDeleted()).count() < exchange.getBotCnt();
  }

  public boolean isExpired() {
    return expireDt != null && expireDt.isBefore(LocalDateTime.now());
  }

  public boolean isNotExpired() {
    return !isExpired();
  }

  public String rawSecretKey() {
    return PasswordEncoderUtils.decrypt(secretKey, accessKey);
  }

  public String rawPassPhrase() {
    return StringUtils.isNotBlank(passPhrase) ? PasswordEncoderUtils.decrypt(passPhrase, accessKey) : passPhrase;
  }

  public ExchangeApiKey getExchangeApiKey() {
    return ExchangeApiKey.builder().accessKey(accessKey).secretKey(rawSecretKey()).passPhrase(rawPassPhrase()).build();
  }

  /**
   * @return 거래소 통화 잔액(거래소에서 사용되는 기본 통화)
   * @throws BizException 거래소 계좌 정보 조회할 수 없는 경우
   */
  public BigDecimal getExchangeCurrency() throws BizException {
    return exchange.getExchangeHandler().getUsableCurrency(getExchangeApiKey(), exchange.getCurrencyCode());
  }

  /**
   * @param coin 확인하려고 하는 코인 정보
   * @return 거래소 사용 가능한 코인 정보
   * @throws BizException 거래소 계좌 정보 조회할 수 없는 경우
   */
  public ExchangeCoinBalanceResponse getExchangeCoinBalance(@NonNull Coin coin) throws BizException {
    ExchangeCoinBalanceResponse response = exchange.getExchangeHandler()
        .getCoinBalance(getExchangeApiKey(), coin.getMarket().getCode(), coin.getCode());

    if (response.getBalance().signum() < 1) {
      response.setBalance(BigDecimal.ZERO);
    }

    return response;
  }

  public List<ExchangeOrderResponse> getExchangeOrders(MarketType marketType, TradingStatus tradingStatus) {
    return exchange.getExchangeHandler().getOrders(getExchangeApiKey(), marketType, tradingStatus);
  }

  public ExchangeWalletResponse getExchangeWallet(String coinCode, String address) {
    return getExchangeWallets(coinCode).stream().filter(f -> f.getAddress().equals(address)).findAny()
        .orElseThrow(() -> new BizException("해당 주소로 등록된 거래소 지갑이 없습니다."));
  }

  public List<ExchangeWalletResponse> getExchangeWallets(String coinCode) {
    List<ExchangeWalletResponse> wallets = exchange.getExchangeHandler().getCoinWallets(getExchangeApiKey(), coinCode);
    if (CollectionUtils.isEmpty(wallets)) {
      throw new BizException("거래소에 등록된 지갑 주소가 없습니다.");
    }

    return wallets;
  }

  public String withdraw(@NonNull ExchangeWithdrawRequest request) {
    return exchange.getExchangeHandler().withdraw(getExchangeApiKey(), request);
  }

  public ApiKeyResponse ofResponse() {
    ApiKeyResponse response = ApiKeyResponse.builder().id(id).keyName(keyName).accessKey(accessKey).secretKey(rawSecretKey()).expireDt(expireDt)
        .userId(account.getUserId()).build();

    if (StringUtils.isNotBlank(passPhrase)) {
      response.setPassPhrase(rawPassPhrase());
    }

    response.setCreatedAndUpdated(createdAndUpdated);

    return response;
  }

  public ApiKeyResponse ofDetailResponse() {
    ApiKeyResponse response = ofResponse();
    response.setExchange(exchange.ofResponse());

    if (CollectionUtils.isNotEmpty(bots)) {
      response.setBots(bots.stream().map(Bot::ofResponse).toList());
    }

    if (CollectionUtils.isNotEmpty(volumeBots)) {
      response.setVolumeBots(volumeBots.stream().map(VolumeBot::ofResponse).toList());
    }

    if (CollectionUtils.isNotEmpty(gridBots)) {
      response.setGridBots(gridBots.stream().map(GridBot::ofResponse).toList());
    }

    if (CollectionUtils.isNotEmpty(listBots)) {
      response.setListBots(listBots.stream().map(ListBot::ofResponse).toList());
    }

    if (CollectionUtils.isNotEmpty(buyBots)) {
      response.setBuyBots(buyBots.stream().map(BuyBot::ofResponse).toList());
    }

    if (CollectionUtils.isNotEmpty(sellBots)) {
      response.setSellBots(sellBots.stream().map(SellBot::ofResponse).toList());
    }

    if (CollectionUtils.isNotEmpty(arbitrageBotApis)) {
      response.setArbitrageBotResponses(arbitrageBotApis.stream().map(f -> f.getArbitrageBot().ofResponse()).toList());
    }

    return response;
  }

  public ApiKeyResponse ofDecryptResponse() {
    ApiKeyResponse response = ofResponse();
    response.setSecretKey(rawSecretKey());

    return response;
  }

  /* =================================================================
   * Business logic
   ================================================================= */
  public void create() {
    account.validCreatableApiKey(exchange);
    checkApiKey();
  }

  public void update(@NonNull ApiKeyUpdate update) {
    if (StringUtils.isNotBlank(update.getKeyName())) {
      keyName = update.getKeyName();
    }
    accessKey = update.getAccessKey();
    secretKey = update.getSecretKey();
    passPhrase = update.getPassPhrase();

    checkApiKey();
  }

  public void delete() {
    bots.forEach(Bot::removeApiKey);
    volumeBots.forEach(VolumeBot::removeApiKey);
    gridBots.forEach(GridBot::removeApiKey);
    listBots.forEach(ListBot::removeApiKey);
    buyBots.forEach(BuyBot::removeApiKey);
    sellBots.forEach(SellBot::removeApiKey);
    arbitrageBotApis.forEach(ArbitrageBotApi::removeApiKey);
    payments.forEach(payment -> payment.setApiKey(null));
  }

  // apikey 정보 거래소 연결 가능 여부 체크 및 유효기간 저장
  public void checkApiKey() {
    expireDt = exchange.getExchangeHandler().getApiKeyExpireAt(getExchangeApiKey());

    if (expireDt != null && expireDt.minusDays(1).isBefore(LocalDateTime.now())) {
      throw new BizException("유효기간이 1일 이하로 남은 API key 는 등록할 수 없습니다.");
    }
  }
}
