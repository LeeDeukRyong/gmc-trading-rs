package com.gmc.trading.infra.support.handler.exchange.upbit.dto;

import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeWalletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpbitCoinAddressResponse {

  private String currency;
  private String net_type;
  private String deposit_address;
  private String secondary_address;

  public ExchangeWalletResponse ofResponse() {
    return ExchangeWalletResponse.builder().coinCode(currency).chain(net_type).address(deposit_address).extraAddress(secondary_address).build();
  }
}
