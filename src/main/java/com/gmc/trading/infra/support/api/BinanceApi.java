package com.gmc.trading.infra.support.api;

import com.gmc.common.exception.BizException;
import com.gmc.common.service.notify.NotifyMessage;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinanceApi {

  @Value("${system.api.binance.url}")
  private String binanceUrl;

  private final NotifyMessage notifyMessage;
  private final RestTemplate restTemplate;

  @SuppressWarnings("unchecked")
  public BigDecimal getPriceUSDT(String coinCode) throws BizException {
    try {
      Map<String, String> result = restTemplate.getForObject(getUrl("/api/v3/ticker/price?symbol=" + coinCode + "USDT"), Map.class);
      return new BigDecimal(Objects.requireNonNull(MapUtils.getString(result, "price")));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      notifyMessage.sendErrorMessage(e);
      throw new BizException("바이넨스 현재가 조회 실패");
    }
  }

  private String getUrl(String path) {
    return binanceUrl + (path.startsWith("/") ? path : "/" + path);
  }
}
