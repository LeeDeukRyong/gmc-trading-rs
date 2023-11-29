package com.gmc.trading.infra.support.scheduler;

import com.gmc.common.utils.RandomUtils;
import com.gmc.trading.modules.buy_bot.application.BuyBotService;
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
public class BuyBotScheduler extends AbstractScheduler {

  private final BuyBotService service;

  @Scheduled(fixedDelayString = "${scheduler.buyBotMonitoring.fixedDelaySecond}", timeUnit = TimeUnit.SECONDS)
  public void scheduledBuyBotMonitoring() {
    service.getWorkingBotList().forEach(buyBot -> {
      long waitSecond = RandomUtils.nextInt(buyBot.getMinCycleSecond(), buyBot.getMaxCycleSecond());
      if (super.lock("scheduledBuyBotMonitoring" + buyBot.getId(), waitSecond, TimeUnit.SECONDS)) {
        scheduler.execute(() -> {
          try {
            TimeUnit.SECONDS.sleep(waitSecond - 3);
            setupToken();
            service.monitoring(buyBot.getId());
          } catch (InterruptedException ie) {
            log.error(ie.getMessage(), ie);
            Thread.currentThread().interrupt();
          }
        });
      }
    });
  }
}