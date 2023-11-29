package com.gmc.trading.modules.volume_bot.application;

import com.gmc.common.dto.common.ResponseDto;
import com.gmc.common.resolver.Search;
import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotCreate;
import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotResponse;
import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotSearchRequest;
import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotSearchResponse;
import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotUpdate;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
@RequestMapping(path = "/volume-bots")
public class VolumeBotController {

  private final VolumeBotService service;

  @GetMapping
  public ResponseEntity<ResponseDto<List<VolumeBotSearchResponse>>> list(@Search VolumeBotSearchRequest request) {
    return ResponseDto.ok(service.list(request));
  }

  @GetMapping(path = "/{id}")
  public ResponseEntity<ResponseDto<VolumeBotResponse>> detail(@PathVariable(value = "id") Long id) {
    return ResponseDto.ok(service.detailResponse(id));
  }

  @PostMapping
  public ResponseEntity<ResponseDto<VolumeBotResponse>> create(@RequestBody @Valid VolumeBotCreate create) {
    return ResponseDto.created(service.create(create));
  }

  @PutMapping(path = "/{id}")
  public ResponseEntity<ResponseDto<VolumeBotResponse>> update(@PathVariable(value = "id") Long id, @RequestBody @Valid VolumeBotUpdate update) {
    return ResponseDto.ok(service.update(id, update));
  }

  @DeleteMapping(path = "/{id}")
  public ResponseEntity<ResponseDto<Void>> delete(@PathVariable(value = "id") Long id) {
    service.delete(id);

    return ResponseDto.ok();
  }
}
