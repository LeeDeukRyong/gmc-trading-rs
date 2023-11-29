package com.gmc.trading.modules.account.application;

import com.gmc.common.dto.common.ResponseDto;
import com.gmc.common.dto.common.SearchRequestDto;
import com.gmc.common.resolver.Search;
import com.gmc.trading.modules.account.application.dto.AccountResponse;
import com.gmc.trading.modules.api_key.application.dto.ApiKeyResponse;
import com.gmc.trading.modules.coin.application.dto.CoinResponse;
import com.gmc.trading.modules.coin.application.dto.CoinSearchRequest;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/accounts")
public class AccountController {

  private final AccountService service;

  @GetMapping(path = "/{userId}")
  public ResponseEntity<ResponseDto<AccountResponse>> detail(@PathVariable(value = "userId") long userId, @Search SearchRequestDto request) {
    return ResponseDto.ok(service.detailResponse(userId, request.getTimeZone()));
  }

  @PreAuthorize("hasAnyRole('SYSTEM', 'ADMIN', 'OPERATOR', 'PLATFORM')")
  @PutMapping(path = "/{userId}/centers/{centerId}")
  public ResponseEntity<ResponseDto<AccountResponse>> updateCenter(@PathVariable(value = "userId") Long userId,
      @PathVariable(value = "centerId") Long centerId) {
    return ResponseDto.ok(service.updateCenter(userId, centerId));
  }

  @GetMapping(path = "/{userId}/trading-coins")
  public ResponseEntity<ResponseDto<List<CoinResponse>>> getTradingCoins(@PathVariable(value = "userId") long userId,
      @Search @Valid CoinSearchRequest request) {
    return ResponseDto.ok(service.getTradingCoins(userId, request));
  }

  @GetMapping(path = "/{userId}/exchanges/{exchangeId}/api-keys")
  public ResponseEntity<ResponseDto<List<ApiKeyResponse>>> getCreatableBotApiKeys(@PathVariable(value = "userId") long userId,
      @PathVariable(value = "exchangeId") long exchangeId) {
    return ResponseDto.ok(service.getCreatableBotApiKeys(userId, exchangeId));
  }
}