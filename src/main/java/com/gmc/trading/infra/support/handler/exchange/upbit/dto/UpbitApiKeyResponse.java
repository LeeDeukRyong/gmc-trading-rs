package com.gmc.trading.infra.support.handler.exchange.upbit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpbitApiKeyResponse {

  private String access_key;
  private String expire_at;
}
