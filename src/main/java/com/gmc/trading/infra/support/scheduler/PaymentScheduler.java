package com.gmc.trading.infra.support.scheduler;

import com.gmc.trading.modules.payment.application.PaymentService;
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
public class PaymentScheduler extends AbstractScheduler {

  private final PaymentService service;

  @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS) // 30초마다
  public void scheduledProcessingPayment() {
    service.getProcessingPaymentIdList().forEach(id -> {
      String key = "scheduledProcessingPayment" + id;
      if (super.lock(key, 1, TimeUnit.MINUTES)) {
        scheduler.execute(() -> {
          try {
            setupToken();
            service.processingPayment(id);
          } finally {
            super.unlock(key);
          }
        });
      }
    });
  }
}
