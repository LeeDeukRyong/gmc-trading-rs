package com.gmc.trading.infra.support.scheduler;

import com.gmc.trading.modules.coin.application.CoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

@Profile("!default")
@Slf4j
@Service
@RequiredArgsConstructor
public class CoinScheduler extends AbstractScheduler {

  private final CoinService service;

  @Scheduled(cron = "0 0 0 * * *") // 매일 0시마다 반복
  @SchedulerLock(name = "scheduledCoinSync", lockAtLeastFor = "PT1H", lockAtMostFor = "PT23H")
  public void scheduledCoinSync() {
    log.info("=============== Start Trading scheduledCoinSync ===============");
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    setupToken();
    service.coinSynchronized();

    stopWatch.stop();
    log.info("=== scheduledCoinSync took: {}", stopWatch.getTotalTimeSeconds());
    log.info("================ End Trading scheduledCoinSync ================");
  }
}
