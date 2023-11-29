package com.gmc.trading.modules.api_key.application;

import com.gmc.common.code.common.CurrencyCode;
import com.gmc.common.code.common.MessageCode;
import com.gmc.common.code.trading.MarketType;
import com.gmc.common.code.trading.TradingSettingType;
import com.gmc.common.code.trading.TradingStatus;
import com.gmc.common.exception.BizException;
import com.gmc.common.utils.SecurityUtils;
import com.gmc.trading.infra.support.api.BinanceApi;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeCoinBalanceResponse;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeOrderResponse;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeWalletResponse;
import com.gmc.trading.modules.account.application.AccountService;
import com.gmc.trading.modules.api_key.application.dto.ApiKeyCreate;
import com.gmc.trading.modules.api_key.application.dto.ApiKeyResponse;
import com.gmc.trading.modules.api_key.application.dto.ApiKeySearchRequest;
import com.gmc.trading.modules.api_key.application.dto.ApiKeySearchResponse;
import com.gmc.trading.modules.api_key.application.dto.ApiKeyUpdate;
import com.gmc.trading.modules.api_key.application.dto.MoneyPaybackQtyRequest;
import com.gmc.trading.modules.api_key.application.dto.MoneyPaybackQtyResponse;
import com.gmc.trading.modules.api_key.domain.ApiKey;
import com.gmc.trading.modules.api_key.infra.ApiKeyMapper;
import com.gmc.trading.modules.api_key.infra.ApiKeyRepository;
import com.gmc.trading.modules.coin.application.CoinService;
import com.gmc.trading.modules.exchange.application.ExchangeService;
import com.gmc.trading.modules.setting.application.SettingService;
import com.gmc.trading.modules.setting.domain.Setting;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ApiKeyService {

  private final ApiKeyRepository repository;
  private final ApiKeyMapper mapper;
  private final AccountService accountService;
  private final ExchangeService exchangeService;
  private final CoinService coinService;
  private final SettingService settingService;
  private final BinanceApi binanceApi;

  @Transactional(readOnly = true)
  public List<ApiKeySearchResponse> list(ApiKeySearchRequest request) {
    return mapper.searchApiKeyList(request);
  }

  public ApiKey detail(long id) {
    ApiKey apiKey = repository.findById(id).orElseThrow(() -> new BizException(MessageCode.NOT_EXIST_DATA));
    if (SecurityUtils.hasRoleOnlyUser() && !apiKey.getAccount().getUserId().equals(SecurityUtils.getUserId())) {
      throw new BizException("API key 정보 접근 권한이 없습니다.");
    }

    return apiKey;
  }

  @Transactional(readOnly = true)
  public ApiKeyResponse detailResponse(long id) {
    return detail(id).ofDetailResponse();
  }

  @Transactional(readOnly = true)
  public ApiKeyResponse decryptResponse(long id) {
    return detail(id).ofDecryptResponse();
  }

  @Transactional
  public ApiKeyResponse create(@NonNull ApiKeyCreate create) {
    ApiKey apiKey = create.ofEntity();
    apiKey.setExchangeAndAccount(exchangeService.detail(create.getExchangeId()), accountService.detail(create.getUserId()));
    apiKey.create();

    return repository.save(apiKey).ofDetailResponse();
  }

  @Transactional
  public ApiKeyResponse update(long id, @NonNull ApiKeyUpdate update) {
    ApiKey apiKey = detail(id);
    apiKey.update(update);

    return apiKey.ofDetailResponse();
  }

  @Transactional
  public void delete(long id) {
    ApiKey apiKey = detail(id);
    apiKey.delete();

    repository.delete(apiKey);
  }

  @Transactional(readOnly = true)
  public MoneyPaybackQtyResponse getMoneyPaybackQty(long id, @NonNull MoneyPaybackQtyRequest request) {
    ApiKey apiKey = detail(id);
    Setting setting = settingService.findByTradingSettingType(TradingSettingType.PAYBACK_FEE);

    BigDecimal money = new BigDecimal(request.getMoney());
    BigDecimal coinUsdtPrice = BigDecimal.ZERO;
    BigDecimal spread = BigDecimal.ZERO;
    BigDecimal qty = money;
    if (!CurrencyCode.USDT.getCode().equalsIgnoreCase(request.getCoinCode())) {
      coinUsdtPrice = binanceApi.getPriceUSDT(request.getCoinCode());
      spread = new BigDecimal(setting.getSettingValue());
      qty = money.divide(coinUsdtPrice.multiply(BigDecimal.ONE.add(spread.multiply(BigDecimal.valueOf(0.01)))), 8, RoundingMode.DOWN);
    }

    String chain = null;
    String address = null;
    String extraAddress = null;

    try {
      ExchangeWalletResponse exchangeWallet = apiKey.getExchangeWallets(request.getCoinCode()).stream()
          .filter(f -> f.getChain().equalsIgnoreCase(apiKey.getExchange().getDefaultPaymentWallet(apiKey.getAccount().getOperatorId()).getChain()))
          .findAny().orElseThrow(() -> new BizException("결제 지갑과 동일한 네트워크를 사용하는 지갑 주소가 없습니다."));
      chain = exchangeWallet.getChain();
      address = exchangeWallet.getAddress();
      extraAddress = exchangeWallet.getExtraAddress();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    return MoneyPaybackQtyResponse.builder().money(money.intValue()).coinCode(request.getCoinCode()).qty(qty).coinUsdtPrice(coinUsdtPrice)
        .spread(spread).chain(chain).address(address).extraAddress(extraAddress).exchange(apiKey.getExchange().ofResponse()).build();
  }

  @Transactional(readOnly = true)
  public BigDecimal getExchangeCurrency(long id) {
    return detail(id).getExchangeCurrency();
  }

  @Transactional(readOnly = true)
  public ExchangeCoinBalanceResponse getExchangeCoinBalance(long id, long coinId) {
    return detail(id).getExchangeCoinBalance(coinService.detail(coinId));
  }

  @Transactional(readOnly = true)
  public List<ExchangeOrderResponse> getExchangeOrders(long id, MarketType marketType, TradingStatus tradingStatus) {
    return detail(id).getExchangeOrders(marketType, tradingStatus);
  }

  @Transactional(readOnly = true)
  public ExchangeOrderResponse cancelOrder(long id, long coinId, String orderId) {
    ApiKey apiKey = detail(id);
    return apiKey.getExchange().getExchangeHandler().orderCancel(apiKey.getExchangeApiKey(), coinService.detail(coinId), orderId);
  }
}
