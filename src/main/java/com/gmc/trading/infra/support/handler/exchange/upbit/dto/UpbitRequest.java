package com.gmc.trading.infra.support.handler.exchange.upbit.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpbitRequest {

  private String accessKey;
  private String secretKey;
  private Map<String, String> params;
}
