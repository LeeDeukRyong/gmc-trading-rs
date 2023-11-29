package com.gmc.trading.modules.coin.application;

import com.gmc.common.dto.common.ResponseDto;
import com.gmc.common.resolver.Search;
import com.gmc.trading.modules.coin.application.dto.CoinResponse;
import com.gmc.trading.modules.coin.application.dto.CoinSearchRequest;
import com.gmc.trading.modules.coin.application.dto.CoinSearchResponse;
import com.gmc.trading.modules.coin.application.dto.CoinUpdate;
import com.gmc.trading.modules.coin.application.dto.OperatorBotCoinSearchRequest;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/coins")
public class CoinController {

  private final CoinService service;

  @GetMapping
  public ResponseEntity<ResponseDto<List<CoinSearchResponse>>> list(@Search CoinSearchRequest request) {
    return ResponseDto.ok(service.list(request));
  }

  @GetMapping(path = "/{id}")
  public ResponseEntity<ResponseDto<CoinResponse>> detail(@PathVariable(value = "id") long id) {
    return ResponseDto.ok(service.detailResponse(id));
  }

  @PreAuthorize("hasAnyRole('SYSTEM')")
  @PutMapping(path = "/{id}")
  public ResponseEntity<ResponseDto<CoinResponse>> update(@PathVariable(value = "id") long id, @RequestBody @Valid CoinUpdate update) {
    return ResponseDto.ok(service.update(id, update));
  }

  @GetMapping(path = "/operators/bot-used")
  public ResponseEntity<ResponseDto<List<CoinSearchResponse>>> getOperatorBotCoins(@Search OperatorBotCoinSearchRequest request) {
    return ResponseDto.ok(service.getOperatorBotCoins(request));
  }

  @PreAuthorize("hasAnyRole('SYSTEM', 'PLATFORM')")
  @PutMapping(path = "/async")
  public ResponseEntity<ResponseDto<Void>> coinSynchronized() {
    service.asyncCoinSynchronized();

    return ResponseDto.ok();
  }
}
