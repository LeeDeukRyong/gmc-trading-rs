package com.gmc.trading.infra.support.scheduler;

import com.gmc.trading.modules.arbitrage_bot.application.ArbitrageBotService;
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
public class ArbitrageBotScheduler extends AbstractScheduler {

  private final ArbitrageBotService service;

  @Scheduled(fixedDelayString = "${scheduler.arbitrageBotMonitoring.fixedDelaySecond}", timeUnit = TimeUnit.SECONDS)
  public void scheduledArbitrageBotMonitoring() {
    service.getWorkingBotIdList().forEach(botId -> {
      if (super.lock("scheduledArbitrageBotMonitoring" + botId, 3, TimeUnit.SECONDS)) {
        scheduler.execute(() -> {
          setupToken();
          service.monitoring(botId);
        });
      }
    });
  }
}
