package com.gmc.trading.infra.support.scheduler;

import com.gmc.common.utils.RandomUtils;
import com.gmc.trading.modules.list_bot.application.ListBotService;
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
public class ListBotScheduler extends AbstractScheduler {

  private final ListBotService service;

  @Scheduled(fixedDelayString = "${scheduler.listBotMonitoring.fixedDelaySecond}", timeUnit = TimeUnit.SECONDS)
  public void scheduledListBotMonitoring() {
    service.getWorkingBotList().forEach(listBot -> {
      long waitSecond = RandomUtils.nextInt(listBot.getMinCycleSecond(), listBot.getMaxCycleSecond());
      if (super.lock("scheduledListBotMonitoring" + listBot.getId(), waitSecond, TimeUnit.SECONDS)) {
        scheduler.execute(() -> {
          try {
            TimeUnit.SECONDS.sleep(waitSecond - 3);
            setupToken();
            service.monitoring(listBot.getId());
          } catch (InterruptedException ie) {
            log.error(ie.getMessage(), ie);
            Thread.currentThread().interrupt();
          }
        });
      }
    });
  }
}