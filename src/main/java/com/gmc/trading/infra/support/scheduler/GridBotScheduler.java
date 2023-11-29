package com.gmc.trading.infra.support.scheduler;

import com.gmc.trading.modules.grid_bot.application.GridBotService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Profile("!default")
@Slf4j
@Service
@RequiredArgsConstructor
public class GridBotScheduler extends AbstractScheduler {

  private final GridBotService service;

  @Scheduled(fixedDelayString = "${scheduler.gridBotMonitoring.fixedDelaySecond}", timeUnit = TimeUnit.SECONDS)
  public void scheduledGridBotMonitoring() {
    service.getWorkingBotIdList().forEach(gridBotId -> {
      if (super.lock("scheduledGridBotMonitoring" + gridBotId, 3, TimeUnit.SECONDS)) {
        scheduler.execute(() -> {
          setupToken();
          service.monitoring(gridBotId);
        });
      }
    });
  }
}