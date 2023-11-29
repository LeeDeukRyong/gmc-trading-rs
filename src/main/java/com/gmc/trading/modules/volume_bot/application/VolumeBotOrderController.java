package com.gmc.trading.modules.volume_bot.application;

import com.gmc.common.dto.common.ResponseDto;
import com.gmc.common.resolver.Search;
import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotOrderSearchRequest;
import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotOrderSearchResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/volume-bot-orders")
public class VolumeBotOrderController {

  private final VolumeBotOrderService service;

  @GetMapping
  public ResponseEntity<ResponseDto<List<VolumeBotOrderSearchResponse>>> list(@Search VolumeBotOrderSearchRequest request) {
    return ResponseDto.ok(service.list(request));
  }
}