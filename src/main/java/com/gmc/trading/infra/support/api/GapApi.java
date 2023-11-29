package com.gmc.trading.infra.support.api;

import com.gmc.common.exception.BizException;
import com.gmc.common.service.notify.NotifyMessage;
import com.gmc.trading.infra.support.api.dto.OrderGapRequest;
import com.gmc.trading.infra.support.api.dto.OrderGapResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class GapApi {

  private final RestTemplate gapRestTemplate;
  private final NotifyMessage notifyMessage;

  public List<OrderGapResponse> getOrderGapList(OrderGapRequest request) {
    String path = "/v2_order_list";

    try {
      return gapRestTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<List<OrderGapResponse>>() {
      }).getBody();
    } catch (Exception e) {
      Map<String, Object> data = new HashMap<>();
      data.put("API URL", gapRestTemplate.getUriTemplateHandler().expand(path).toString());
      data.put("request", request.toString());

      log.error(data.toString(), e);
      notifyMessage.sendErrorMessage(e, data);

      throw new BizException("간격 목록을 불러올 수 없습니다.");
    }
  }
}
