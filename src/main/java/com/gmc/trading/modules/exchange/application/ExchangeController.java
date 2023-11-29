package com.gmc.trading.modules.exchange.application;

import com.gmc.common.dto.common.ResponseDto;
import com.gmc.common.resolver.Search;
import com.gmc.trading.modules.exchange.application.dto.ExchangeGapUpdate;
import com.gmc.trading.modules.exchange.application.dto.ExchangeOperatorDto;
import com.gmc.trading.modules.exchange.application.dto.ExchangeResponse;
import com.gmc.trading.modules.exchange.application.dto.ExchangeSearchRequest;
import com.gmc.trading.modules.exchange.application.dto.ExchangeSearchResponse;
import com.gmc.trading.modules.exchange.application.dto.ExchangeUpdate;
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
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/exchanges")
public class ExchangeController {

  private final ExchangeService service;

  @GetMapping
  public ResponseEntity<ResponseDto<List<ExchangeSearchResponse>>> list(@Search ExchangeSearchRequest request) {
    return ResponseDto.ok(service.list(request));
  }

  @GetMapping(path = "/{id}")
  public ResponseEntity<ResponseDto<ExchangeResponse>> detail(@PathVariable(value = "id") long id) {
    return ResponseDto.ok(service.detailResponse(id));
  }

  @PreAuthorize("hasAnyRole('SYSTEM', 'ADMIN')")
  @PutMapping(path = "/{id}")
  public ResponseEntity<ResponseDto<ExchangeResponse>> update(@PathVariable(value = "id") long id, @RequestBody @Valid ExchangeUpdate update) {
    return ResponseDto.ok(service.update(id, update));
  }

  @PreAuthorize("hasAnyRole('SYSTEM', 'ADMIN')")
  @PutMapping(path = "/{id}/gaps")
  public ResponseEntity<ResponseDto<ExchangeResponse>> updateGap(@PathVariable(value = "id") long id,
      @RequestBody @Valid ExchangeGapUpdate update) {
    return ResponseDto.ok(service.updateGap(id, update));
  }

  @PreAuthorize("hasAnyRole('SYSTEM', 'ADMIN')")
  @PostMapping(path = "/{id}/operators")
  public ResponseEntity<ResponseDto<ExchangeResponse>> createOperator(@PathVariable(value = "id") long id,
      @RequestBody @Valid ExchangeOperatorDto exchangeOperatorDto) {
    return ResponseDto.created(service.createOperator(id, exchangeOperatorDto));
  }

  @PreAuthorize("hasAnyRole('SYSTEM', 'ADMIN')")
  @DeleteMapping(path = "/{id}/operators")
  public ResponseEntity<ResponseDto<ExchangeResponse>> deleteOperator(@PathVariable(value = "id") long id,
      @RequestBody @Valid ExchangeOperatorDto exchangeOperatorDto) {
    return ResponseDto.ok(service.deleteOperator(id, exchangeOperatorDto));
  }
}
