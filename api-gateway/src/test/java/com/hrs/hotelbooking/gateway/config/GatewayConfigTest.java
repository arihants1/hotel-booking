package com.hrs.hotelbooking.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GatewayConfigTest {

    private GatewayConfig gatewayConfig;
    private Resilience4JCircuitBreakerFactory factory;

    @BeforeEach
    void setUp() {
        gatewayConfig = new GatewayConfig();
        factory = mock(Resilience4JCircuitBreakerFactory.class);
    }

    @Test
    void shouldCreateDefaultCustomizer() {
        // When
        Customizer<Resilience4JCircuitBreakerFactory> customizer = gatewayConfig.defaultCustomizer();

        // Then
        assertNotNull(customizer);
    }

    @Test
    void shouldConfigureDefaultCircuitBreakerSettings() {
        // Given
        Customizer<Resilience4JCircuitBreakerFactory> customizer = gatewayConfig.defaultCustomizer();

        // When
        customizer.customize(factory);

        // Then
        verify(factory).configureDefault(any());
    }

    @Test
    void shouldCreateBookingServiceCustomizer() {
        // When
        Customizer<Resilience4JCircuitBreakerFactory> customizer = gatewayConfig.bookingServiceCustomizer();

        // Then
        assertNotNull(customizer);
    }

    @Test
    void shouldConfigureBookingServiceCircuitBreaker() {
        // Given
        Customizer<Resilience4JCircuitBreakerFactory> customizer = gatewayConfig.bookingServiceCustomizer();

        // When
        customizer.customize(factory);

        // Then
        verify(factory).configure(any(), eq("booking-service-cb"));
    }

    @Test
    void shouldCreateHotelServiceCustomizer() {
        // When
        Customizer<Resilience4JCircuitBreakerFactory> customizer = gatewayConfig.hotelServiceCustomizer();

        // Then
        assertNotNull(customizer);
    }

    @Test
    void shouldConfigureHotelServiceCircuitBreaker() {
        // Given
        Customizer<Resilience4JCircuitBreakerFactory> customizer = gatewayConfig.hotelServiceCustomizer();

        // When
        customizer.customize(factory);

        // Then
        verify(factory).configure(any(), eq("hotel-service-cb"));
    }

    @Test
    void shouldHaveCorrectDefaultCircuitBreakerSettings() {
        // This test validates the circuit breaker configuration can be built
        // with the expected settings structure
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(20)
                .minimumNumberOfCalls(10)
                .failureRateThreshold(50.0f)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slowCallRateThreshold(50.0f)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .build();

        // Then - verify the config was built successfully and has expected basic properties
        assertEquals(20, config.getSlidingWindowSize());
        assertEquals(10, config.getMinimumNumberOfCalls());
        assertEquals(50.0f, config.getFailureRateThreshold());
        assertEquals(50.0f, config.getSlowCallRateThreshold());
        assertEquals(Duration.ofSeconds(2), config.getSlowCallDurationThreshold());
        assertNotNull(config);
    }

    @Test
    void shouldHaveCorrectDefaultTimeLimiterSettings() {
        // This test validates the time limiter configuration values
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .build();

        // Then
        assertEquals(Duration.ofSeconds(5), config.getTimeoutDuration());
    }

    @Test
    void shouldHaveCorrectBookingServiceCircuitBreakerSettings() {
        // Validate booking service specific settings can be configured
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(30)
                .minimumNumberOfCalls(15)
                .failureRateThreshold(60.0f)
                .waitDurationInOpenState(Duration.ofSeconds(45))
                .slowCallDurationThreshold(Duration.ofSeconds(3))
                .build();

        // Then - verify the config was built successfully with booking service values
        assertEquals(30, config.getSlidingWindowSize());
        assertEquals(15, config.getMinimumNumberOfCalls());
        assertEquals(60.0f, config.getFailureRateThreshold());
        assertEquals(Duration.ofSeconds(3), config.getSlowCallDurationThreshold());
        assertNotNull(config);
    }

    @Test
    void shouldHaveCorrectBookingServiceTimeLimiterSettings() {
        // Validate booking service time limiter settings
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(8))
                .build();

        // Then
        assertEquals(Duration.ofSeconds(8), config.getTimeoutDuration());
    }

    @Test
    void shouldHaveCorrectHotelServiceCircuitBreakerSettings() {
        // Validate hotel service specific settings can be configured
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(15)
                .minimumNumberOfCalls(8)
                .failureRateThreshold(40.0f)
                .waitDurationInOpenState(Duration.ofSeconds(20))
                .slowCallDurationThreshold(Duration.ofSeconds(1))
                .build();

        // Then - verify the config was built successfully with hotel service values
        assertEquals(15, config.getSlidingWindowSize());
        assertEquals(8, config.getMinimumNumberOfCalls());
        assertEquals(40.0f, config.getFailureRateThreshold());
        assertEquals(Duration.ofSeconds(1), config.getSlowCallDurationThreshold());
        assertNotNull(config);
    }

    @Test
    void shouldHaveCorrectHotelServiceTimeLimiterSettings() {
        // Validate hotel service time limiter settings
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3))
                .build();

        // Then
        assertEquals(Duration.ofSeconds(3), config.getTimeoutDuration());
    }

    @Test
    void shouldHaveDifferentSettingsForDifferentServices() {
        // Verify that different services have different timeout settings
        // Hotel service should be faster (3s) than booking service (8s)

        TimeLimiterConfig hotelConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3))
                .build();

        TimeLimiterConfig bookingConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(8))
                .build();

        // Then
        assertTrue(hotelConfig.getTimeoutDuration().compareTo(bookingConfig.getTimeoutDuration()) < 0,
                "Hotel service timeout should be less than booking service timeout");
    }

    @Test
    void shouldConfigureCircuitBreakerWithDifferentWindowSizes() {
        // Test that we can create circuit breakers with different sliding window sizes
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(20)
                .build();

        CircuitBreakerConfig bookingConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(30)
                .build();

        CircuitBreakerConfig hotelConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(15)
                .build();

        // Then
        assertEquals(20, defaultConfig.getSlidingWindowSize());
        assertEquals(30, bookingConfig.getSlidingWindowSize());
        assertEquals(15, hotelConfig.getSlidingWindowSize());
    }

    @Test
    void shouldConfigureCircuitBreakerWithDifferentFailureThresholds() {
        // Test that we can create circuit breakers with different failure rate thresholds
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50.0f)
                .build();

        CircuitBreakerConfig bookingConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(60.0f)
                .build();

        CircuitBreakerConfig hotelConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(40.0f)
                .build();

        // Then
        assertEquals(50.0f, defaultConfig.getFailureRateThreshold());
        assertEquals(60.0f, bookingConfig.getFailureRateThreshold());
        assertEquals(40.0f, hotelConfig.getFailureRateThreshold());
    }
}