package com.gmc.trading.infra.support.scheduler;

import com.gmc.common.utils.RandomUtils;
import com.gmc.trading.modules.volume_bot.application.VolumeBotService;
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
public class VolumeBotScheduler extends AbstractScheduler {

  private final VolumeBotService service;

  @Scheduled(fixedDelayString = "${scheduler.volumeBotMonitoring.fixedDelaySecond}", timeUnit = TimeUnit.SECONDS)
  public void scheduledVolumeBotMonitoring() {
    service.getWorkingBotList().forEach(volumeBot -> {
      long waitSecond = RandomUtils.nextInt(volumeBot.getMinCycleSecond(), volumeBot.getMaxCycleSecond());
      if (super.lock("scheduledVolumeBotMonitoring" + volumeBot.getId(), waitSecond, TimeUnit.SECONDS)) {
        scheduler.execute(() -> {
          try {
            TimeUnit.SECONDS.sleep(waitSecond - 3);
            setupToken();
            service.monitoring(volumeBot.getId());
          } catch (InterruptedException ie) {
            log.error(ie.getMessage(), ie);
            Thread.currentThread().interrupt();
          }
        });
      }
    });
  }
}
