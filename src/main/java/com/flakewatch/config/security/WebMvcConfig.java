package com.flakewatch.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApiKeyRateLimitInterceptor apiKeyRateLimitInterceptor;

    public WebMvcConfig(ApiKeyRateLimitInterceptor apiKeyRateLimitInterceptor) {
        this.apiKeyRateLimitInterceptor = apiKeyRateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiKeyRateLimitInterceptor)
                .addPathPatterns("/api/v1/ingest/**");
    }
}
