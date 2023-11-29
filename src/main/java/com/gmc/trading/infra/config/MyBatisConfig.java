package com.gmc.trading.infra.config;

import com.gmc.common.interceptor.GridPagingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisConfig {

  @Bean
  public GridPagingInterceptor gridPagingInterceptor() {
    return new GridPagingInterceptor();
  }
}
