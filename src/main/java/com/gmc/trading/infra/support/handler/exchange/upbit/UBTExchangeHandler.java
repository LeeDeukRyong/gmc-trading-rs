package com.gmc.trading.infra.support.handler.exchange.upbit;

import static java.util.stream.Collectors.toSet;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.gmc.common.code.common.CurrencyCode;
import com.gmc.common.code.trading.MarketType;
import com.gmc.common.code.trading.OrderType;
import com.gmc.common.code.trading.TradingStatus;
import com.gmc.common.exception.BizException;
import com.gmc.trading.infra.support.handler.exchange.AbstractExchangeHandler;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeApiKey;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeCoinBalanceResponse;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeCoinResponse;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeOrderBookResponse;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeOrderResponse;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeTradeResponse;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeWalletResponse;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeWithdrawRequest;
import com.gmc.trading.infra.support.handler.exchange.upbit.dto.UpbitAccountResponse;
import com.gmc.trading.infra.support.handler.exchange.upbit.dto.UpbitApiKeyResponse;
import com.gmc.trading.infra.support.handler.exchange.upbit.dto.UpbitCoinAddressResponse;
import com.gmc.trading.infra.support.handler.exchange.upbit.dto.UpbitCoinResponse;
import com.gmc.trading.infra.support.handler.exchange.upbit.dto.UpbitErrorResponse;
import com.gmc.trading.infra.support.handler.exchange.upbit.dto.UpbitOrderBookResponse;
import com.gmc.trading.infra.support.handler.exchange.upbit.dto.UpbitOrderResponse;
import com.gmc.trading.infra.support.handler.exchange.upbit.dto.UpbitRequest;
import com.gmc.trading.infra.support.handler.exchange.upbit.dto.UpbitTradeResponse;
import com.gmc.trading.infra.support.handler.exchange.upbit.dto.UpbitWithdrawResponse;
import com.gmc.trading.modules.bot.domain.Bot;
import com.gmc.trading.modules.coin.domain.Coin;
import com.gmc.trading.modules.mode.domain.Mode;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.springframework.web.client.HttpStatusCodeException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UBTExchangeHandler extends AbstractExchangeHandler {

  @Value("${system.api.upbit.url}")
  private String url;

  private Algorithm getAlgorithm(@NonNull String secretKey) {
    return Algorithm.HMAC256(secretKey);
  }

  private Builder getJwtBuilder(@NonNull String accessKey) {
    return JWT.create().withClaim("access_key", accessKey).withClaim("nonce", UUID.randomUUID().toString());
  }

  private String generatorToken(@NonNull UpbitRequest request) {
    try {
      Builder builder = getJwtBuilder(request.getAccessKey());
      String queryString = getQueryString(request.getParams());

      if (StringUtils.isNotBlank(queryString)) {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes(StandardCharsets.UTF_8));

        builder.withClaim("query_hash", String.format("%0128x", new BigInteger(1, md.digest())));
        builder.withClaim("query_hash_alg", "SHA512");
      }

      return "Bearer " + builder.sign(getAlgorithm(request.getSecretKey()));
    } catch (Exception e) {
      throw new BizException("업비트 API token 생성 실패");
    }
  }

  private MultiValueMap<String, String> getHeaders(@NonNull UpbitRequest request) {
    MultiValueMap<String, String> headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");
    headers.set("Authorization", generatorToken(request));

    return headers;
  }

  private String getQueryString(Map<String, String> params) {
    if (MapUtils.isNotEmpty(params)) {
      List<String> queryElements = new ArrayList<>();
      for (Map.Entry<String, String> entity : params.entrySet()) {
        queryElements.add(entity.getKey() + "=" + entity.getValue());
      }

      return String.join("&", queryElements.toArray(new String[0]));
    }

    return "";
  }

  private UpbitRequest converterRequest(@NonNull ExchangeApiKey apiKey, Map<String, String> params) {
    return UpbitRequest.builder().accessKey(apiKey.getAccessKey()).secretKey(apiKey.getSecretKey()).params(params).build();
  }

  @Override
  protected <T> ResponseEntity<T> resultHandle(@NonNull ResponseEntity<T> responseEntity, @NonNull String path) {
    String remainingReq = responseEntity.getHeaders().getFirst("Remaining-Req");

    //group=order;min=199;sec=29 -- private api (/v1/orders)
    //group=default;min=899;sec=29 -- private api (/v1/deposits, /v1/api_keys, /v1/accounts, /v1/order)
    //group=market;min=599;sec=9 -- /v1/market/all 조회
    //group=ticker;min=599;sec=9 -- /v1/ticker 조회  => 사용하면 안될듯. 대체 -> 웹소켓으로 시세 받아서 레디스에 넣어서 사용
    //group=crix-trades;min=599;sec9 -- /v1/trades/ticks

    try {
      if (StringUtils.isNotBlank(remainingReq)) {
        String[] parts = remainingReq.split("; ");
        String group = parts[0].split("=")[1];
        int min = Integer.parseInt(parts[1].split("=")[1]);
        int sec = Integer.parseInt(parts[2].split("=")[1]);
        boolean isSleep = false;

        if (group.equalsIgnoreCase("market") || group.equalsIgnoreCase("ticker") || group.equalsIgnoreCase("crix-trades")) {
          if (sec < 3) {
            isSleep = true;
            TimeUnit.SECONDS.sleep(1);
          }
        } else {
          if (sec < 10) {
            isSleep = true;
            TimeUnit.SECONDS.sleep(1);
          }
        }

        if (min < 30) {
          isSleep = true;
          TimeUnit.SECONDS.sleep(3);
        }

        if (isSleep && sec < 4) {
          sendApiLimitMessage(path, remainingReq, responseEntity.getBody());
        }
      }
    } catch (InterruptedException ie) {
      log.warn(ie.getMessage(), ie);
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    return responseEntity;
  }

  @Override
  protected String getUrl(@NonNull String path) {
    return url + (path.startsWith("/") ? path : "/" + path);
  }

  @Override
  protected BigDecimal getFee(@NonNull Coin coin) {
    // 업비트는 매수한 금액에서 현금으로 수수료 차감하기 때문에 수수료 포함하지 않음.
    return BigDecimal.ZERO;
  }

  @Override
  public BigDecimal getQuoteUnit(@NonNull Coin coin, @NonNull BigDecimal price) {
    if (price.compareTo(BigDecimal.valueOf(0.1)) < 0) {
      return BigDecimal.valueOf(0.0001);
    } else if (price.compareTo(BigDecimal.valueOf(1)) < 0) {
      return BigDecimal.valueOf(0.001);
    } else if (price.compareTo(BigDecimal.valueOf(10)) < 0) {
      return BigDecimal.valueOf(0.01);
    } else if (price.compareTo(BigDecimal.valueOf(100)) < 0) {
      return BigDecimal.valueOf(0.1);
    } else if (price.compareTo(BigDecimal.valueOf(1_000)) < 0) {
      return BigDecimal.valueOf(1);
    } else if (price.compareTo(BigDecimal.valueOf(10_000)) < 0) {
      return BigDecimal.valueOf(5);
    } else if (price.compareTo(BigDecimal.valueOf(100_000)) < 0) {
      return BigDecimal.valueOf(10);
    } else if (price.compareTo(BigDecimal.valueOf(500_000)) < 0) {
      return BigDecimal.valueOf(50);
    } else if (price.compareTo(BigDecimal.valueOf(1_000_000)) < 0) {
      return BigDecimal.valueOf(100);
    } else if (price.compareTo(BigDecimal.valueOf(2_000_000)) < 0) {
      return BigDecimal.valueOf(500);
    } else {
      return BigDecimal.valueOf(1_000);
    }
  }

  @Override
  public String getExchangeName() {
    return "upbit";
  }

  @Override
  public String converterMarket(@NonNull Coin coin) {
    return String.join("-", coin.getMarket().getCode(), coin.getCode());
  }

  @Override
  public BigDecimal getLiquidationPrice(@NonNull ExchangeApiKey apiKey, @NonNull Coin coin, @NonNull Mode mode) {
    return BigDecimal.ZERO;
  }

  @Override
  public BigDecimal getCoinPrice(@NonNull Coin coin) throws BizException {
    try {
      List<Map<String, Object>> coinPrices = send("/v1/ticker?markets=" + converterMarket(coin), HttpMethod.GET, HttpEntity.EMPTY,
          new ParameterizedTypeReference<>() {
          });

      return new BigDecimal(MapUtils.getString(coinPrices.get(0), "trade_price"));
    } catch (Exception e) {
      throw new BizException("UPBIT 시세 현재가를 조회할 수 없습니다.");
    }
  }

  @Override
  public Set<ExchangeCoinResponse> getCoins(@NonNull MarketType marketType, @NonNull String marketCode) throws BizException {
    try {
      Set<UpbitCoinResponse> coins = send("/v1/market/all?isDetails=true", HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<>() {
      });

      return coins.stream().map(UpbitCoinResponse::ofResponse).collect(toSet());
    } catch (Exception e) {
      throw new BizException("UPBIT 코인 목록을 조회할 수 없습니다.");
    }
  }

  @Override
  public ExchangeTradeResponse getRecentTrade(@NonNull Coin coin) throws BizException {
    try {
      List<UpbitTradeResponse> trades = send("/v1/trades/ticks?market=" + converterMarket(coin) + "&count=1", HttpMethod.GET, HttpEntity.EMPTY,
          new ParameterizedTypeReference<>() {
          });

      return trades.stream().map(UpbitTradeResponse::ofResponse).findAny().orElseThrow();
    } catch (Exception e) {
      throw new BizException("UPBIT 최근 체결 정보를 조회할 수 없습니다.");
    }
  }

  @Override
  public List<ExchangeOrderBookResponse> getOrderBook(@NonNull Coin coin) throws BizException {
    try {
      String market = converterMarket(coin);
      List<UpbitOrderBookResponse> response = send("/v1/orderbook?markets=" + market, HttpMethod.GET, HttpEntity.EMPTY,
          new ParameterizedTypeReference<>() {
          });

      return response.stream().filter(f -> f.getMarket().equalsIgnoreCase(market)).findAny()
          .orElseGet(UpbitOrderBookResponse.builder().orderbook_units(new ArrayList<>())::build).toList();
    } catch (Exception e) {
      throw new BizException("UPBIT 오더북을 조회할 수 없습니다.");
    }
  }

  @Override
  public void checkAccountConfig(@NonNull Bot bot) throws BizException {
  }

  @Override
  public void changeLeverage(@NonNull Bot bot) throws BizException {
  }

  private List<UpbitAccountResponse> getAccounts(@NonNull ExchangeApiKey apiKey) throws BizException {
    try {
      return send("/v1/accounts", HttpMethod.GET, new HttpEntity<>(getHeaders(converterRequest(apiKey, null))), new ParameterizedTypeReference<>() {
      });
    } catch (Exception e) {
      throw new BizException("UPBIT 계좌 정보를 조회할 수 없습니다.");
    }
  }

  @Override
  public ExchangeCoinBalanceResponse getCoinBalance(@NonNull ExchangeApiKey apiKey, @NonNull String marketCode, @NonNull String coinCode)
      throws BizException {
    return getAccounts(apiKey).stream().filter(f -> coinCode.equalsIgnoreCase(f.getCurrency()) && marketCode.equalsIgnoreCase(f.getUnit_currency()))
        .findFirst().map(UpbitAccountResponse::ofResponse)
        .orElseGet(() -> ExchangeCoinBalanceResponse.builder().currency(coinCode).balance(BigDecimal.ZERO).avgBuyPrice(BigDecimal.ZERO).build());
  }

  @Override
  public BigDecimal getUsableCurrency(@NonNull ExchangeApiKey apiKey, @NonNull CurrencyCode currencyCode) throws BizException {
    return getAccounts(apiKey).stream().filter(f -> f.getCurrency().equalsIgnoreCase(currencyCode.getCode())).findFirst()
        .map(UpbitAccountResponse::getBalance).orElse(BigDecimal.ZERO);
  }

  @Override
  public LocalDateTime getApiKeyExpireAt(@NonNull ExchangeApiKey apiKey) throws BizException {
    try {
      Set<UpbitApiKeyResponse> apiKeys = send("/v1/api_keys", HttpMethod.GET, new HttpEntity<>(getHeaders(converterRequest(apiKey, null))),
          new ParameterizedTypeReference<>() {
          });

      return apiKeys.stream().filter(f -> f.getAccess_key().equals(apiKey.getAccessKey())).map(
          upbitkey -> ZonedDateTime.parse(upbitkey.getExpire_at(), DateTimeFormatter.ISO_DATE_TIME).withZoneSameInstant(ZoneId.systemDefault())
              .toLocalDateTime()).findAny().orElseThrow();
    } catch (Exception e) {
      throw new BizException("UPBIT API Key 검증에 실패 하였습니다.");
    }
  }

  @Override
  public List<ExchangeWalletResponse> getCoinWallets(@NonNull ExchangeApiKey apiKey, @NonNull String coinCode) throws BizException {
    try {
      List<UpbitCoinAddressResponse> wallets = send("/v1/deposits/coin_addresses", HttpMethod.GET,
          new HttpEntity<>(getHeaders(converterRequest(apiKey, null))), new ParameterizedTypeReference<>() {
          });

      return wallets.stream().filter(f -> f.getCurrency().equalsIgnoreCase(coinCode)).map(UpbitCoinAddressResponse::ofResponse).toList();
    } catch (Exception e) {
      String message = "UPBIT 지갑 주소를 조회할 수 없습니다.";
      if (e instanceof NotFound notFound) {
        try {
          message = objectMapper.readValue(notFound.getResponseBodyAsString(), UpbitErrorResponse.class).getError().getMessage();
        } catch (Exception ex) {
          log.error("UPBIT 오류 메시지 컨버터 실패");
        }
      }

      throw new BizException(message);
    }
  }

  @Override
  public ExchangeOrderResponse limitBuy(@NonNull ExchangeApiKey apiKey, @NonNull Coin coin, Mode mode, @NonNull BigDecimal coinPrice,
      @NonNull BigDecimal qty) throws BizException {
    try {
      OrderType orderType = OrderType.BUY;
      return createOrder(getOrderParam(coin, orderType, convertQuote(orderType, coin, coinPrice), qty), apiKey).ofResponse();
    } catch (Exception e) {
      throw new BizException("UPBIT 지정가 매수 실패");
    }
  }

  @Override
  public ExchangeOrderResponse limitSell(@NonNull ExchangeApiKey apiKey, @NonNull Coin coin, Mode mode, @NonNull BigDecimal coinPrice,
      @NonNull BigDecimal qty) throws BizException {
    try {
      OrderType orderType = OrderType.SELL;
      return createOrder(getOrderParam(coin, orderType, convertQuote(orderType, coin, coinPrice), qty), apiKey).ofResponse();
    } catch (Exception e) {
      throw new BizException("UPBIT 지정가 매도 실패");
    }
  }

  @Override
  public ExchangeOrderResponse marketBuy(@NonNull ExchangeApiKey apiKey, @NonNull Coin coin, Mode mode, @NonNull BigDecimal amount,
      boolean withOutFee) {
    try {
      if (withOutFee) {
        amount = coin.getExchange().withOutFee(amount);
      }

      return checkOrderComplete(apiKey, coin, createOrder(getOrderParam(coin, OrderType.BUY, amount, null), apiKey).getUuid());
    } catch (Exception e) {
      throw new BizException("UPBIT 시장가 매수 실패");
    }
  }

  @Override
  public ExchangeOrderResponse marketSell(@NonNull ExchangeApiKey apiKey, @NonNull Coin coin, Mode mode, @NonNull BigDecimal qty) {
    try {
      return checkOrderComplete(apiKey, coin, createOrder(getOrderParam(coin, OrderType.SELL, null, qty), apiKey).getUuid());
    } catch (Exception e) {
      throw new BizException("UPBIT 시장가 매도 실패");
    }
  }

  private UpbitOrderResponse createOrder(@NonNull Map<String, String> param, @NonNull ExchangeApiKey apiKey) {
    return send("/v1/orders", HttpMethod.POST, new HttpEntity<>(param, getHeaders(converterRequest(apiKey, param))),
        new ParameterizedTypeReference<>() {
        });
  }

  private Map<String, String> getOrderParam(@NonNull Coin coin, @NonNull OrderType orderType, BigDecimal price, BigDecimal orderQty) {
    Map<String, String> param = new HashMap<>();
    param.put("market", converterMarket(coin));
    param.put("side", OrderType.BUY.equals(orderType) ? "bid" : "ask");

    if (orderQty != null) {
      param.put("volume", orderQty.stripTrailingZeros().toPlainString());
    }

    if (price != null) {
      param.put("price", price.stripTrailingZeros().toPlainString());
    }

    if (orderQty != null && price != null) {
      param.put("ord_type", "limit");
    } else {
      param.put("ord_type", orderQty == null ? "price" : "market");
    }

    return param;
  }

  @Override
  public ExchangeOrderResponse orderCancel(@NonNull ExchangeApiKey apiKey, @NonNull Coin coin, @NonNull String orderId) throws BizException {
    int cnt = 1;
    while (cnt != 4) {
      try {
        Map<String, String> params = Map.of("uuid", orderId);

        send("/v1/order?" + getQueryString(params), HttpMethod.DELETE,
            new HttpEntity<>(getHeaders(converterRequest(apiKey, params))), new ParameterizedTypeReference<>() {
            });

        ExchangeOrderResponse orderCancel = checkOrderComplete(apiKey, coin, orderId);
        orderCancel.setTradingStatus(TradingStatus.COMPLETE_TRADING);

        return orderCancel;
      } catch (Exception e) {
        // 이미 체결완료 되었거나, 취소된 주문
        if (e instanceof HttpClientErrorException && ((HttpStatusCodeException) e).getStatusCode().equals(HttpStatus.NOT_FOUND)) {
          return checkOrderComplete(apiKey, coin, orderId);
        }

        try {
          TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException ie) {
          log.warn(ie.getMessage(), ie);
          Thread.currentThread().interrupt();
        }

        ++cnt;
      }
    }

    throw new BizException("UPBIT 주문 취소 실패");
  }

  @Override
  public ExchangeOrderResponse getOrder(@NonNull ExchangeApiKey apiKey, @NonNull Coin coin, @NonNull String orderId) throws BizException {
    try {
      Map<String, String> params = Map.of("uuid", orderId);

      UpbitOrderResponse order = send("/v1/order?" + getQueryString(params), HttpMethod.GET,
          new HttpEntity<>(getHeaders(converterRequest(apiKey, params))), new ParameterizedTypeReference<>() {
          });

      return order.ofResponse();
    } catch (Exception e) {
      throw new BizException("UPBIT 주문 정보 확인 실패");
    }
  }

  @Override
  public List<ExchangeOrderResponse> getOrders(@NonNull ExchangeApiKey apiKey, MarketType marketType, TradingStatus tradingStatus)
      throws BizException {
    try {
      Map<String, String> params = Map.of("state", tradingStatus == null ? "wait" : switch (tradingStatus) {
        case COMPLETE_TRADING -> "done";
        case MANUAL_CANCEL -> "cancel";
        default -> "wait";
      });
      List<UpbitOrderResponse> orders = send("/v1/orders?" + getQueryString(params), HttpMethod.GET,
          new HttpEntity<>(getHeaders(converterRequest(apiKey, params))), new ParameterizedTypeReference<>() {
          });

      return orders.stream().map(UpbitOrderResponse::ofResponse).toList();
    } catch (Exception e) {
      String message = "UPBIT 주문 조회 실패";
      log.error(e.getMessage(), e);

      try {
        if (e instanceof HttpClientErrorException error) {
          message = objectMapper.readValue(error.getResponseBodyAsString(), UpbitErrorResponse.class).getError().getMessage();
        }
      } catch (Exception e2) {
        log.error("UPBIT 오류 메시지 컨버터 실패");
      }
      throw new BizException(message);
    }
  }

  @Override
  public ExchangeOrderResponse getLiquidationOrder(@NonNull ExchangeApiKey apiKey, @NonNull Coin coin, @NonNull String position,
      @NonNull BigDecimal qty) throws BizException {
    throw new BizException("처리할 수 없는 요청 입니다.");
  }

  @Override
  public String withdraw(@NonNull ExchangeApiKey apiKey, @NonNull ExchangeWithdrawRequest request) throws BizException {
    try {
      Map<String, String> params = new HashMap<>();
      params.put("currency", request.getCoinCode());
      params.put("net_type", request.getChain());
      params.put("amount", request.getQty().stripTrailingZeros().toPlainString());
      params.put("address", request.getAddress());
      params.put("transaction_type", "internal");

      if (StringUtils.isNotBlank(request.getExtraAddress())) {
        params.put("secondary_address", request.getExtraAddress());
      }

      UpbitWithdrawResponse withdraw = send("/v1/withdraws/coin", HttpMethod.POST,
          new HttpEntity<>(params, getHeaders(converterRequest(apiKey, params))), new ParameterizedTypeReference<>() {
          });

      return withdraw.getUuid();
    } catch (Exception e) {
      String message = "UPBIT 출금 실패 - 관리자에게 문의 하세요.";
      if (e instanceof BadRequest badRequest) {
        try {
          String errorName = objectMapper.readValue(badRequest.getResponseBodyAsString(), UpbitErrorResponse.class).getError().getName();

          message = switch (errorName) {
            case "withdraw_address_not_registered" ->
              // 등록된 출금 주소가 아닙니다.
                "디지털 출금주소 관리에 출금허용주소를 등록했는지 확인해주세요.<br><p style=\"color:red\">출금주소 등록 후 다시 진행해주세요.<br>매수된 코인 수량은 직접 정리하셔야 합니다.</p>";
            case "withdraw_first_fiat_limit" ->
              // KRW 첫 입금시점부터 72시간 동안 모든 디지털 자산 출금 제한
                "UPBIT의 출금 지연 제도 도입으로 첫 입금시점으로부터 72시간 동안 모든 디지털 자산 출금이 지연됩니다.<br><p style=\"color:red\">첫 원화 입급 72시간 이후 다시 진행해주세요.<br>매수된 코인 수량은 직접 정리하셔야 합니다.</p>";
            case "withdraw_delay_time_amount" ->
              // 출금 지연제 적용에 따라 24시간 이내 입금한 원한 입금액 상당의 코인 출금 제한
                "UPBIT의 출금 지연 제도 도입으로 첫 입금시점으로부터 24시간 동안 모든 디지털 자산 출금이 지연됩니다.<br><p style=\"color:red\">원화 입급 24시간 이후 다시 진행해주세요.<br>매수된 코인 수량은 직접 정리하셔야 합니다.</p>";
            default -> message;
          };
        } catch (Exception ex) {
          log.error("UPBIT 오류 메시지 컨버터 실패");
        }
      }

      throw new BizException(message);
    }
  }

  @Override
  public boolean isWithdrawComplete(@NonNull ExchangeApiKey apiKey, @NonNull String exchangeWithdrawId) throws BizException {
    try {
      Map<String, String> params = new HashMap<>();
      params.put("uuid", exchangeWithdrawId);

      UpbitWithdrawResponse withdraw = send("/v1/withdraw?" + getQueryString(params), HttpMethod.GET,
          new HttpEntity<>(params, getHeaders(converterRequest(apiKey, params))), new ParameterizedTypeReference<>() {
          });

      return withdraw.getState().equalsIgnoreCase("DONE");
    } catch (Exception e) {
      throw new BizException("UPBIT 출금 조회 실패");
    }
  }
}