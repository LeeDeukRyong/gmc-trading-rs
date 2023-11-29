package com.gmc.trading.infra.config;

import com.gmc.common.config.AsyncTaskExecutorConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

@Profile("!test")
@Configuration
@EnableAsync
public class AsyncConfig extends AsyncTaskExecutorConfigurer {

  @Primary
  @Bean
  public TaskExecutor taskExecutor() {
    return super.delegatingSecurityContextAsyncTaskExecutor(30, 100, 1_000, 20, "asyncTask");
  }
}
