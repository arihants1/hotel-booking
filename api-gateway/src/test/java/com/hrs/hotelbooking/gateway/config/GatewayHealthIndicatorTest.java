package com.hrs.hotelbooking.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatewayHealthIndicatorTest {

    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    @Mock
    private Mono<String> redisMono;

    private GatewayHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new GatewayHealthIndicator(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldReturnHealthyStatusWhenRedisIsAccessible() {
        // Given
        when(valueOperations.get("hrs:gateway:health:ping")).thenReturn(redisMono);
        when(redisMono.timeout(Duration.ofSeconds(2))).thenReturn(redisMono);
        when(redisMono.block()).thenReturn("OK");

        // When
        Health health = healthIndicator.health();

        // Then
        assertEquals(Status.UP, health.getStatus());
        assertEquals("UP", health.getDetails().get("hrs-gateway"));
        assertEquals("OK", health.getDetails().get("redis-connectivity"));
        assertEquals("OPERATIONAL", health.getDetails().get("rate-limiter"));
        assertEquals("1.0.0", health.getDetails().get("version"));
        assertNotNull(health.getDetails().get("timestamp"));
    }

    @Test
    void shouldReturnUnhealthyStatusWhenRedisIsNotAccessible() {
        // Given
        RuntimeException redisException = new RuntimeException("Redis connection failed");
        when(valueOperations.get("hrs:gateway:health:ping")).thenReturn(redisMono);
        when(redisMono.timeout(Duration.ofSeconds(2))).thenReturn(redisMono);
        when(redisMono.block()).thenThrow(redisException);

        // When
        Health health = healthIndicator.health();

        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("DOWN", health.getDetails().get("hrs-gateway"));
        assertEquals("FAILED", health.getDetails().get("redis-connectivity"));
        assertEquals("Redis connection failed", health.getDetails().get("error"));
        assertNotNull(health.getDetails().get("timestamp"));
    }

    @Test
    void shouldReturnUnhealthyStatusWhenRedisTimesOut() {
        // Given
        RuntimeException timeoutException = new RuntimeException("Redis operation timed out");
        when(valueOperations.get("hrs:gateway:health:ping")).thenReturn(redisMono);
        when(redisMono.timeout(Duration.ofSeconds(2))).thenReturn(redisMono);
        when(redisMono.block()).thenThrow(timeoutException);

        // When
        Health health = healthIndicator.health();

        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("DOWN", health.getDetails().get("hrs-gateway"));
        assertEquals("FAILED", health.getDetails().get("redis-connectivity"));
        assertNotNull(health.getDetails().get("error"));
        assertTrue(health.getDetails().get("error").toString().contains("timed out"));
    }

    @Test
    void shouldHandleNullRedisResponse() {
        // Given
        when(valueOperations.get("hrs:gateway:health:ping")).thenReturn(redisMono);
        when(redisMono.timeout(Duration.ofSeconds(2))).thenReturn(redisMono);
        when(redisMono.block()).thenReturn(null);

        // When
        Health health = healthIndicator.health();

        // Then
        assertEquals(Status.UP, health.getStatus());
        assertEquals("UP", health.getDetails().get("hrs-gateway"));
        assertEquals("OK", health.getDetails().get("redis-connectivity"));
    }

    @Test
    void shouldUseCorrectRedisKey() {
        // Given
        when(valueOperations.get("hrs:gateway:health:ping")).thenReturn(redisMono);
        when(redisMono.timeout(Duration.ofSeconds(2))).thenReturn(redisMono);
        when(redisMono.block()).thenReturn("pong");

        // When
        healthIndicator.health();

        // Then
        verify(valueOperations).get("hrs:gateway:health:ping");
    }

    @Test
    void shouldApplyCorrectTimeout() {
        // Given
        when(valueOperations.get("hrs:gateway:health:ping")).thenReturn(redisMono);
        when(redisMono.timeout(Duration.ofSeconds(2))).thenReturn(redisMono);
        when(redisMono.block()).thenReturn("OK");

        // When
        healthIndicator.health();

        // Then
        verify(redisMono).timeout(Duration.ofSeconds(2));
    }

    @Test
    void shouldIncludeAllRequiredHealthDetails() {
        // Given
        when(valueOperations.get("hrs:gateway:health:ping")).thenReturn(redisMono);
        when(redisMono.timeout(Duration.ofSeconds(2))).thenReturn(redisMono);
        when(redisMono.block()).thenReturn("OK");

        // When
        Health health = healthIndicator.health();

        // Then
        assertTrue(health.getDetails().containsKey("hrs-gateway"));
        assertTrue(health.getDetails().containsKey("redis-connectivity"));
        assertTrue(health.getDetails().containsKey("rate-limiter"));
        assertTrue(health.getDetails().containsKey("timestamp"));
        assertTrue(health.getDetails().containsKey("version"));
    }

    @Test
    void shouldHandleInterruptedException() {
        // Given
        RuntimeException interruptedException = new RuntimeException("Thread was interrupted during Redis operation");
        when(valueOperations.get("hrs:gateway:health:ping")).thenReturn(redisMono);
        when(redisMono.timeout(Duration.ofSeconds(2))).thenReturn(redisMono);
        when(redisMono.block()).thenThrow(interruptedException);

        // When
        Health health = healthIndicator.health();

        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().get("error").toString().contains("interrupted"));
    }

    @Test
    void shouldHandleGenericRedisException() {
        // Given
        RuntimeException redisException = new RuntimeException("Redis cluster connection lost");
        when(valueOperations.get("hrs:gateway:health:ping")).thenReturn(redisMono);
        when(redisMono.timeout(Duration.ofSeconds(2))).thenReturn(redisMono);
        when(redisMono.block()).thenThrow(redisException);

        // When
        Health health = healthIndicator.health();

        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("DOWN", health.getDetails().get("hrs-gateway"));
        assertEquals("FAILED", health.getDetails().get("redis-connectivity"));
        assertEquals("Redis cluster connection lost", health.getDetails().get("error"));
        assertNotNull(health.getDetails().get("timestamp"));
    }

    @Test
    void shouldBlockOnMonoExecution() {
        // Given
        when(valueOperations.get("hrs:gateway:health:ping")).thenReturn(redisMono);
        when(redisMono.timeout(Duration.ofSeconds(2))).thenReturn(redisMono);
        when(redisMono.block()).thenReturn("OK");

        // When
        Health health = healthIndicator.health();

        // Then
        verify(redisMono).block();
        assertEquals(Status.UP, health.getStatus());
    }
}
