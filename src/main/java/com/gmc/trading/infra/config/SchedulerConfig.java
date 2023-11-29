package com.gmc.trading.infra.config;

import com.gmc.common.config.SchedulerConfigurer;
import com.gmc.common.utils.ApplicationContextUtils;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT1M")
public class SchedulerConfig extends SchedulerConfigurer {

  @Bean
  public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
    return super.threadPoolTaskScheduler(100, 20, "scheduler");
  }

  @Bean
  public LockProvider lockProvider(RedisConnectionFactory redisConnectionFactory) {
    return new RedisLockProvider(redisConnectionFactory, ApplicationContextUtils.getActiveProfile());
  }
}
