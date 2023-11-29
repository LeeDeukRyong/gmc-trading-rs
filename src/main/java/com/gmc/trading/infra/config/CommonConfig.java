package com.gmc.trading.infra.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmc.common.config.CommonConfigurer;
import com.gmc.common.service.cache.AdminCacheService;
import com.gmc.common.service.cache.CacheMessageSource;
import com.gmc.common.service.notify.NotifyMessage;
import com.gmc.common.support.api.AdminApi;
import com.gmc.common.support.api.MmsApi;
import com.gmc.common.support.api.PointApi;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.LocaleResolver;

@Configuration
public class CommonConfig extends CommonConfigurer {

  @Value("${system.slack.url}")
  private String slackWebHookUrl;

  @Value("${system.slack.channel}")
  private String slackChannel;

  @Value("${system.date-format}")
  private String systemDateFormat;

  @Bean
  public CommonsRequestLoggingFilter requestLoggingFilter() {
    CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
    loggingFilter.setIncludeClientInfo(true);
    loggingFilter.setIncludeQueryString(true);
    loggingFilter.setIncludeHeaders(true);
    loggingFilter.setIncludePayload(true);

    return loggingFilter;
  }

  @Bean
  public LocaleResolver localeResolver() {
    return customAcceptHeaderLocaleResolver("lang", List.of(Locale.KOREAN, Locale.ENGLISH, Locale.CHINESE), Locale.KOREAN);
  }

  @Bean
  public ObjectMapper objectMapper() {
    String dateFormat = StringUtils.isBlank(systemDateFormat) ? "yyyy-MM-dd" : systemDateFormat;
    return super.customObjectMapper(dateFormat, dateFormat + " HH:mm:ss");
  }

  @Bean
  public NotifyMessage notifyMessage() {
    return notifyMessage(slackChannel, slackWebHookUrl);
  }

  @Bean
  public AdminCacheService adminCacheService(@Qualifier(value = "adminRestTemplate") RestTemplate adminRestTemplate, CacheManager cacheManager) {
    return super.adminCacheService(adminRestTemplate, cacheManager);
  }

  @Bean(name = "messageSource")
  public CacheMessageSource messageSource(AdminCacheService adminCacheService) {
    return super.cacheMessageSource(adminCacheService);
  }

  @Bean
  public AdminApi adminApi(@Qualifier(value = "adminRestTemplate") RestTemplate adminRestTemplate) {
    return new AdminApi(adminRestTemplate);
  }

  @Bean
  public PointApi pointApi(@Qualifier(value = "pointRestTemplate") RestTemplate pointRestTemplate) {
    return new PointApi(pointRestTemplate);
  }

  @Bean
  public MmsApi mmsApi(@Qualifier(value = "mmsRestTemplate") RestTemplate mmsRestTemplate) {
    return new MmsApi(mmsRestTemplate);
  }
}
