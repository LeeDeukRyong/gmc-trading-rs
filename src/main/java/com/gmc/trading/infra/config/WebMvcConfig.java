package com.gmc.trading.infra.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmc.common.resolver.SearchArgumentResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

  private final ObjectMapper objectMapper;

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    // controller list 검색 시 파라메터 dto converter 시 objectMapper 적용
    // 해당 controller 파라메터 dto 앞에 @Search 어노테이션 적용해야 함.
    SearchArgumentResolver searchArgumentResolver = new SearchArgumentResolver();
    searchArgumentResolver.setObjectMapper(objectMapper);

    resolvers.add(searchArgumentResolver);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/*/**")
        .allowedOriginPatterns("*")
        .allowedMethods("*")
        .allowedHeaders("*")
        .allowCredentials(true)
        .maxAge(3600L);
  }
}
