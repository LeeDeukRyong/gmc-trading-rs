package com.gmc.trading.modules.api_key.application;

import com.gmc.common.code.trading.MarketType;
import com.gmc.common.code.trading.TradingStatus;
import com.gmc.common.dto.common.ResponseDto;
import com.gmc.common.resolver.Search;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeCoinBalanceResponse;
import com.gmc.trading.infra.support.handler.exchange.dto.ExchangeOrderResponse;
import com.gmc.trading.modules.api_key.application.dto.ApiKeyCreate;
import com.gmc.trading.modules.api_key.application.dto.ApiKeyResponse;
import com.gmc.trading.modules.api_key.application.dto.ApiKeySearchRequest;
import com.gmc.trading.modules.api_key.application.dto.ApiKeySearchResponse;
import com.gmc.trading.modules.api_key.application.dto.ApiKeyUpdate;
import com.gmc.trading.modules.api_key.application.dto.MoneyPaybackQtyRequest;
import com.gmc.trading.modules.api_key.application.dto.MoneyPaybackQtyResponse;
import java.math.BigDecimal;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api-keys")
public class ApiKeyController {

  private final ApiKeyService service;

  @GetMapping
  public ResponseEntity<ResponseDto<List<ApiKeySearchResponse>>> list(@Search ApiKeySearchRequest request) {
    return ResponseDto.ok(service.list(request));
  }

  @GetMapping(path = "/{id}")
  public ResponseEntity<ResponseDto<ApiKeyResponse>> detail(@PathVariable(value = "id") long id) {
    return ResponseDto.ok(service.detailResponse(id));
  }

  @PreAuthorize("hasAnyRole('SYSTEM')")
  @GetMapping(path = "/{id}/decrypt")
  public ResponseEntity<ResponseDto<ApiKeyResponse>> decrypt(@PathVariable(value = "id") long id) {
    return ResponseDto.ok(service.decryptResponse(id));
  }

  @PostMapping
  public ResponseEntity<ResponseDto<ApiKeyResponse>> create(@RequestBody @Valid ApiKeyCreate create) {
    return ResponseDto.created(service.create(create));
  }

  @PutMapping(path = "/{id}")
  public ResponseEntity<ResponseDto<ApiKeyResponse>> update(@PathVariable(value = "id") long id, @RequestBody @Valid ApiKeyUpdate update) {
    return ResponseDto.ok(service.update(id, update));
  }

  @DeleteMapping(path = "/{id}")
  public ResponseEntity<ResponseDto<Void>> delete(@PathVariable(value = "id") long id) {
    service.delete(id);

    return ResponseDto.ok();
  }

  @GetMapping(path = "/{id}/money-payback/qty")
  public ResponseEntity<ResponseDto<MoneyPaybackQtyResponse>> getMoneyPaybackQty(@PathVariable(value = "id") long id,
      @Search @Valid MoneyPaybackQtyRequest request) {
    return ResponseDto.ok(service.getMoneyPaybackQty(id, request));
  }

  @GetMapping(path = "/{id}/currency")
  public ResponseEntity<ResponseDto<BigDecimal>> getExchangeCurrency(@PathVariable(value = "id") long id) {
    return ResponseDto.ok(service.getExchangeCurrency(id));
  }

  @GetMapping(path = "/{id}/balance/coins/{coinId}")
  public ResponseEntity<ResponseDto<ExchangeCoinBalanceResponse>> getExchangeCoinBalance(@PathVariable(value = "id") long id,
      @PathVariable(value = "coinId") long coinId) {
    return ResponseDto.ok(service.getExchangeCoinBalance(id, coinId));
  }

  @PreAuthorize("hasAnyRole('SYSTEM')")
  @GetMapping(path = "/{id}/orders")
  public ResponseEntity<ResponseDto<List<ExchangeOrderResponse>>> getExchangeOrders(@PathVariable(value = "id") long id,
      @RequestParam(value = "marketType", required = false) MarketType marketType,
      @RequestParam(value = "tradingStatus", required = false) TradingStatus tradingStatus) {
    return ResponseDto.ok(service.getExchangeOrders(id, marketType, tradingStatus));
  }

  @PreAuthorize("hasAnyRole('SYSTEM')")
  @DeleteMapping(path = "/{id}/coins/{coinId}/orders/{orderId}")
  public ResponseEntity<ResponseDto<ExchangeOrderResponse>> cancelOrder(@PathVariable(value = "id") long id,
      @PathVariable(value = "coinId") long coinId, @PathVariable(value = "orderId") String orderId) {
    return ResponseDto.ok(service.cancelOrder(id, coinId, orderId));
  }
}
