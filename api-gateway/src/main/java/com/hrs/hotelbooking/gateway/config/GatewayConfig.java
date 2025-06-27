package com.hrs.hotelbooking.gateway.config;

import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

import java.time.Duration;

/**
 * HRS Gateway Configuration
 * Configures circuit breakers, timeouts, and resilience patterns
 * Optimized for high-throughput HRS booking operations
 * 
 * @author arihants1
 */
@Configuration
public class GatewayConfig {

    /**
     * Configure circuit breaker for HRS services
     * Prevents cascading failures and provides fallback mechanisms
     */
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .slidingWindowSize(20) // Number of calls to track
                        .minimumNumberOfCalls(10) // Min calls before circuit can open
                        .failureRateThreshold(50.0f) // Failure rate threshold (%)
                        .waitDurationInOpenState(Duration.ofSeconds(30)) // Wait before half-open
                        .slowCallRateThreshold(50.0f) // Slow call rate threshold (%)
                        .slowCallDurationThreshold(Duration.ofSeconds(2)) // What's considered slow
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(5)) // Request timeout
                        .build())
                .build());
    }

    /**
     * Custom circuit breaker for booking service
     * More lenient settings due to complex booking operations
     */
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> bookingServiceCustomizer() {
        return factory -> factory.configure(builder -> builder
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .slidingWindowSize(30)
                        .minimumNumberOfCalls(15)
                        .failureRateThreshold(60.0f)
                        .waitDurationInOpenState(Duration.ofSeconds(45))
                        .slowCallDurationThreshold(Duration.ofSeconds(3))
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(8))
                        .build()), 
                "booking-service-cb");
    }

    /**
     * Custom circuit breaker for hotel service
     * Faster settings for search operations
     */
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> hotelServiceCustomizer() {
        return factory -> factory.configure(builder -> builder
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .slidingWindowSize(15)
                        .minimumNumberOfCalls(8)
                        .failureRateThreshold(40.0f)
                        .waitDurationInOpenState(Duration.ofSeconds(20))
                        .slowCallDurationThreshold(Duration.ofSeconds(1))
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(3))
                        .build()), 
                "hotel-service-cb");
    }
}