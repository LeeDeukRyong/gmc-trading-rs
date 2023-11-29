package com.gmc.trading.infra.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmc.common.config.Oauth2RestTemplateConfigurer;
import com.gmc.common.interceptor.OAuth2ClientCredentialsAuthorizedInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig extends Oauth2RestTemplateConfigurer {

  @Value("${system.restTemplate.connection-time-out}")
  private Integer connectTimeOut;

  @Value("${system.restTemplate.read-time-out}")
  private Integer readTimeOut;

  @Value("${system.api.gmc.admin.url}")
  private String gmcAdminUrl;

  @Value("${system.api.gmc.point.url}")
  private String gmcPointUrl;

  @Value("${system.api.gmc.mms.url}")
  private String mmsUrl;

  @Value("${system.api.gmc.gap.url}")
  private String gmcGapUrl;

  public RestTemplateConfig(ObjectMapper objectMapper) {
    super(objectMapper);
  }

  @Bean(name = "restTemplate")
  RestTemplate restTemplate() {
    return restTemplate(connectTimeOut, readTimeOut);
  }

  @Bean(name = "adminRestTemplate")
  RestTemplate adminRestTemplate(ClientHttpRequestInterceptor oauth2ClientInterceptor) {
    return oauthRestTemplate(connectTimeOut, readTimeOut, gmcAdminUrl, oauth2ClientInterceptor);
  }

  @Bean(name = "pointRestTemplate")
  RestTemplate pointRestTemplate(ClientHttpRequestInterceptor oauth2ClientInterceptor) {
    return oauthRestTemplate(connectTimeOut, readTimeOut, gmcPointUrl, oauth2ClientInterceptor);
  }

  @Bean(name = "mmsRestTemplate")
  RestTemplate mmsRestTemplate(ClientHttpRequestInterceptor oauth2ClientInterceptor) {
    return oauthRestTemplate(connectTimeOut, readTimeOut, mmsUrl, oauth2ClientInterceptor);
  }

  @Bean(name = "gapRestTemplate")
  RestTemplate gapRestTemplate() {
    return restTemplate(connectTimeOut, readTimeOut, gmcGapUrl);
  }

  @Bean(name = "oauth2ClientInterceptor")
  ClientHttpRequestInterceptor oauth2ClientInterceptor(ClientRegistrationRepository clientRegistrationRepository) {
    return new OAuth2ClientCredentialsAuthorizedInterceptor(clientRegistrationRepository.findByRegistrationId("gmc"));
  }
}
