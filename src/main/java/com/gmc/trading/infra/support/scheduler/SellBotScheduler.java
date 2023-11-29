package com.gmc.trading.infra.support.scheduler;

import com.gmc.common.utils.RandomUtils;
import com.gmc.trading.modules.sell_bot.application.SellBotService;
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
public class SellBotScheduler extends AbstractScheduler {

  private final SellBotService service;

  @Scheduled(fixedDelayString = "${scheduler.sellBotMonitoring.fixedDelaySecond}", timeUnit = TimeUnit.SECONDS)
  public void scheduledSellBotMonitoring() {
    service.getWorkingBotList().forEach(sellBot -> {
      long waitSecond = RandomUtils.nextInt(sellBot.getMinCycleSecond(), sellBot.getMaxCycleSecond());
      if (super.lock("scheduledSellBotMonitoring" + sellBot.getId(), waitSecond, TimeUnit.SECONDS)) {
        scheduler.execute(() -> {
          try {
            TimeUnit.SECONDS.sleep(waitSecond - 3);
            setupToken();
            service.monitoring(sellBot.getId());
          } catch (InterruptedException ie) {
            log.error(ie.getMessage(), ie);
            Thread.currentThread().interrupt();
          }
        });
      }
    });
  }
}