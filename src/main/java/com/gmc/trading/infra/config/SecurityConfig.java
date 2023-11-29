package com.gmc.trading.infra.config;

import com.gmc.common.security.CustomJwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsUtils;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  @Bean
  SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
    // @formatter:off
    http.httpBasic().disable().formLogin().disable().csrf().disable();
    http.cors().and().authorizeRequests().requestMatchers(CorsUtils::isPreFlightRequest).permitAll();
    http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    http.authorizeRequests().antMatchers("/actuator/**", "/docs/**").permitAll();
    http.authorizeRequests().anyRequest().authenticated();
    http.oauth2ResourceServer().jwt().jwtAuthenticationConverter(new CustomJwtAuthenticationConverter());

    SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL);
    // @formatter:on

    return http.build();
  }
}