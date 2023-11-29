package com.gmc.trading.infra.support.scheduler;

import com.gmc.trading.modules.maintenance_deposit.application.MaintenanceDepositService;
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
public class MaintenanceDepositScheduler extends AbstractScheduler {

  private final MaintenanceDepositService service;

  @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS) // 30초마다
  public void scheduledProcessingMaintenanceDeposit() {
    service.getProcessingMaintenanceDepositIdList().forEach(id -> {
      String key = "scheduledProcessingMaintenanceDeposit" + id;
      if (super.lock(key, 1, TimeUnit.MINUTES)) {
        scheduler.execute(() -> {
          try {
            setupToken();
            service.processingMaintenanceDeposit(id);
          } finally {
            super.unlock(key);
          }
        });
      }
    });
  }
}
