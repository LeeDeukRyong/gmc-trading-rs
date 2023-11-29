package com.gmc.trading.infra.config;

import com.gmc.common.config.RedisCacheConfigurer;
import com.gmc.common.redis.aop.AopForTransaction;
import com.gmc.common.redis.aop.DistributedLockAop;
import com.gmc.common.redis.service.ProcessLockService;
import com.gmc.common.redis.service.RedisCoinService;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisCacheConfig extends RedisCacheConfigurer {

  @Value("${spring.redis-coin.host}")
  private String redisCoinHost;

  @Value("${spring.redis-coin.port}")
  private int redisCoinPort;
  private final RedisProperties redisProperties;

  public RedisCacheConfig(RedisProperties redisProperties) {
    this.redisProperties = redisProperties;
  }

  @Bean
  public RedissonClient redissonClient() {
    return super.redissonClientSingle(redisProperties);
  }

  @Primary
  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return super.redissonConnectionFactory(redissonClient());
  }

  @Bean
  public CacheManager cacheManager() {
    return super.cacheManager(redisConnectionFactory(), redisProperties.getTimeout());
  }

  @Bean
  public ProcessLockService processLockService() {
    return super.processLockService(redissonClient());
  }

  @Bean
  public AopForTransaction aopForTransaction() {
    return super.aopForTransaction();
  }

  @Bean
  public DistributedLockAop distributedLockAop() {
    return super.distributedLockAop(processLockService(), aopForTransaction());
  }

  @Bean
  public RedisConnectionFactory redisCoinConnectionFactory() {
    return super.redissonConnectionFactory(redisCoinHost, redisCoinPort);
  }

  @Bean
  public RedisCoinService redisCoinService() {
    return new RedisCoinService(super.redisTemplate(redisCoinConnectionFactory()));
  }
}