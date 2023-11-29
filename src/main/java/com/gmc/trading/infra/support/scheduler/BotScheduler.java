package com.gmc.trading.infra.support.scheduler;

import com.gmc.trading.modules.bot.application.BotService;
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
public class BotScheduler extends AbstractScheduler {

  private final BotService service;

  @Scheduled(fixedDelayString = "${scheduler.botMonitoring.fixedDelaySecond}", timeUnit = TimeUnit.SECONDS)
  public void scheduledBotMonitoring() {
    service.getWorkingBotIdList().forEach(botId -> {
      if (super.lock("scheduledBotMonitoring" + botId, 3, TimeUnit.SECONDS)) {
        scheduler.execute(() -> {
          setupToken();
          service.monitoring(botId);
        });
      }
    });
  }
}
