package com.gmc.trading.modules.volume_bot.application;

import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotOrderSearchRequest;
import com.gmc.trading.modules.volume_bot.application.dto.VolumeBotOrderSearchResponse;
import com.gmc.trading.modules.volume_bot.infra.VolumeBotOrderMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class VolumeBotOrderService {

  private final VolumeBotOrderMapper mapper;

  @Transactional(readOnly = true)
  public List<VolumeBotOrderSearchResponse> list(VolumeBotOrderSearchRequest request) {
    return mapper.searchVolumeBotOrderList(request);
  }
}
