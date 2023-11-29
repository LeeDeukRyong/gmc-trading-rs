package com.gmc.trading.infra.support.scheduler;

import com.gmc.common.code.oauth2.ClientRoleCode;
import com.gmc.common.redis.service.ProcessLockService;
import com.gmc.common.utils.SecurityUtils;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Slf4j
public abstract class AbstractScheduler {

  @Autowired
  protected ThreadPoolTaskScheduler scheduler;
  @Autowired
  private ProcessLockService processLockService;

  // crontab
  // 초(0-59) 분(0-59) 시(0-23) 일(1-31) 월(1-12) 요일(0-7) 년도(생략가능)
  // 요일에서 0과 7은 일요일이며, 1부터 월요일 6이 토요일
  // ex) "0 0/10 * * * *" -> 0초, 0/10 10분 마다, * 모든시, * 모든 일, * 모든 월, * 모든 요일
  // scheduler
  // lockAtMostFor -> 작업을 진행 중인 노드가 소멸될 경우에도 lock 이 유지될 시간
  // lockAtLeastFor -> 작업이 lock 되어야 할 최소한 시간
  // Lock 시간은 보통 실행 주기 -1 을 권장

  protected void setupToken() {
    SecurityUtils.setUser("SCHEDULER", ClientRoleCode.PLATFORM_TRADING.getCode());
  }

  protected boolean lock(@NonNull String lockKey, long leaseTime, @NonNull TimeUnit timeUnit) {
    return processLockService.lock(lockKey, leaseTime, timeUnit);
  }

  protected void unlock(@NonNull String lockKey) {
    processLockService.unlock(lockKey);
  }
}
