package com.gmc.trading.infra.support.handler.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmc.common.code.trading.OrderType;
import com.gmc.common.exception.BizException;
import com.gmc.common.service.notify.NotifyMessage;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeApiKey;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeOrderResponse;
import com.gmc.trading.modules.coin.domain.Coin;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
public abstract class AbstractExchangeHandler implements ExchangeHandler {

  @Autowired
  protected NotifyMessage notifyMessage;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  protected ObjectMapper objectMapper;

  /**
   * API 호출
   */
  protected <T> T send(String path, HttpMethod method, HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType,
      Object... uriVariables) throws RestClientException {
    try {
      ResponseEntity<T> responseEntity = restTemplate.exchange(getUrl(path), method, requestEntity, responseType, uriVariables);
      if (responseEntity.getStatusCode().isError()) {
        throw new HttpClientErrorException(responseEntity.getStatusCode());
      }

      return resultHandle(responseEntity, path).getBody();
    } catch (Exception e) {
      errorHandle(e);

      Map<String, Object> data = new HashMap<>();
      data.put("API URL", UriComponentsBuilder.fromUriString(getUrl(path)).buildAndExpand(uriVariables).toUriString());
      data.put("request", requestEntity.toString());

      log.error(data.toString(), e);
      notifyMessage.sendErrorMessage(e, data);

      throw e;
    }
  }

  /**
   * API 호출 결과 처리
   */
  protected abstract <T> ResponseEntity<T> resultHandle(@NonNull ResponseEntity<T> responseEntity, @NonNull String path);

  /**
   * API 호출 오류 처리
   *
   * @param e 오류
   */
  protected void errorHandle(@NonNull Exception e) {
    if (e instanceof BadRequest badRequest && badRequest.getStatusCode().equals(HttpStatus.TOO_MANY_REQUESTS)) {
      sleep(1_000);
    }
  }

  protected void sleep(long milliSecond) {
    try {
      TimeUnit.MILLISECONDS.sleep(milliSecond);
    } catch (InterruptedException ie) {
      log.warn(ie.getMessage(), ie);
      Thread.currentThread().interrupt();
    }
  }

  protected void sendApiLimitMessage(@NonNull String path, @NonNull String limitInfo, Object body) {
    Map<String, Object> data = new HashMap<>();
    data.put("Exchange", getExchangeName());
    data.put("LimitInfo", limitInfo);
    if (body != null) {
      data.put("Response Body", body);
    }

    notifyMessage.sendMessage(data);
    log.info("{} Exchange Api Limit Path {} Info: {}", getExchangeName(), path, limitInfo);
  }

  /**
   * 거래소 API URL
   */
  protected abstract String getUrl(@NonNull String path);

  @Override
  public BigDecimal getStopLossPrice(@NonNull Coin coin, @NonNull BigDecimal stopLossTargetAmount, @NonNull BigDecimal tradingQty) {
    // 손절가 = 손절목표금액 / 거래 수량
    BigDecimal stopLossPrice = stopLossTargetAmount.divide(tradingQty, 15, RoundingMode.HALF_UP);
    // 거래소 호가단위
    BigDecimal quoteUnit = getQuoteUnit(coin, stopLossPrice);

    // 호가 적용 = 손절가 / 호가단위 * 호가단위
    return stopLossPrice.divide(quoteUnit, 0, RoundingMode.DOWN).multiply(quoteUnit);
  }

  @Override
  public BigDecimal getBuyQty(@NonNull Coin coin, @NonNull BigDecimal sellTradingQty, @NonNull BigDecimal buyTradingQty) {
    return sellTradingQty.divide(BigDecimal.ONE.subtract(getFee(coin)), coin.getLotSz().scale(), RoundingMode.DOWN).subtract(buyTradingQty);
  }

  protected abstract BigDecimal getFee(@NonNull Coin coin);

  protected BigDecimal convertQuote(@NonNull OrderType orderType, @NonNull Coin coin, @NonNull BigDecimal coinPrice) {
    BigDecimal quoteUnit = getQuoteUnit(coin, coinPrice);

    // 호가변환 금액 = 금액 / 호가단위 * 호가단위
    if (OrderType.BUY.equals(orderType)) {
      // 매수는 단위 버림
      return coinPrice.divide(quoteUnit, 0, RoundingMode.DOWN).multiply(quoteUnit);
    } else {
      // 매도는 단위 올림
      return coinPrice.divide(quoteUnit, 0, RoundingMode.UP).multiply(quoteUnit);
    }
  }

  protected ExchangeOrderResponse checkOrderComplete(@NonNull ExchangeApiKey apiKey, @NonNull Coin coin, @NotNull String orderId) {
    int cnt = 1;
    while (cnt != 10) {
      try {
        TimeUnit.MILLISECONDS.sleep(500);

        ExchangeOrderResponse order = getOrder(apiKey, coin, orderId);
        if (order.isComplete()) {
          return order;
        }
      } catch (InterruptedException ie) {
        log.warn(ie.getMessage(), ie);
        Thread.currentThread().interrupt();
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }

      ++cnt;
    }

    throw new BizException("주문 상태 완료 여부 확인 실패");
  }
}