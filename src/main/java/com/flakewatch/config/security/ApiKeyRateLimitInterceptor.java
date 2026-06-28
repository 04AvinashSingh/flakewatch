package com.flakewatch.config.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiKeyRateLimitInterceptor implements HandlerInterceptor {

    private static final String API_KEY_HEADER = "X-API-KEY";

    @Value("${flakewatch.security.api-keys}")
    private List<String> validApiKeys;

    @Value("${flakewatch.security.rate-limit.requests-per-minute}")
    private int requestsPerMinute;

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey == null || !validApiKeys.contains(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or missing X-API-KEY");
            return false;
        }

        Bucket bucket = cache.computeIfAbsent(apiKey, this::createNewBucket);

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            response.setStatus(429); // Too Many Requests
            response.getWriter().write("Rate limit exceeded for API Key");
            return false;
        }
    }

    private Bucket createNewBucket(String apiKey) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(requestsPerMinute)
                .refillGreedy(requestsPerMinute, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
