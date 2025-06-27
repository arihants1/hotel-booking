package com.hrs.hotelbooking.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * HRS Gateway Health Indicator
 * Custom health check for HRS API Gateway
 * Monitors Redis connectivity and gateway status
 * 
 * @author arihants1
 * @since 2025-06-26 19:23:24 UTC
 */
@Component("hrsGateway")
@Slf4j
public class GatewayHealthIndicator implements HealthIndicator {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public GatewayHealthIndicator(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Health health() {
        try {
            // Test Redis connectivity for rate limiting
            String pingResult = redisTemplate.opsForValue()
                    .get("hrs:gateway:health:ping")
                    .timeout(Duration.ofSeconds(2))
                    .block();
            
            return Health.up()
                    .withDetail("hrs-gateway", "UP")
                    .withDetail("redis-connectivity", "OK")
                    .withDetail("rate-limiter", "OPERATIONAL")
                    .withDetail("timestamp", java.time.LocalDateTime.now())
                    .withDetail("version", "1.0.0")
                    .build();
                    
        } catch (Exception e) {
            log.error("HRS Gateway health check failed", e);
            return Health.down()
                    .withDetail("hrs-gateway", "DOWN")
                    .withDetail("error", e.getMessage())
                    .withDetail("redis-connectivity", "FAILED")
                    .withDetail("timestamp", java.time.LocalDateTime.now())
                    .build();
        }
    }
}