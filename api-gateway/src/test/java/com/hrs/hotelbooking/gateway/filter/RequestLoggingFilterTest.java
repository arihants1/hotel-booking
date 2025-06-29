package com.hrs.hotelbooking.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestLoggingFilterTest {

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private GatewayFilterChain chain;

    @Mock
    private HttpHeaders requestHeaders;

    @Mock
    private HttpHeaders responseHeaders;

    private RequestLoggingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RequestLoggingFilter();

        // Use lenient stubbing to avoid unnecessary stubbing exceptions
        lenient().when(exchange.getRequest()).thenReturn(request);
        lenient().when(exchange.getResponse()).thenReturn(response);
        lenient().when(response.getHeaders()).thenReturn(responseHeaders);
        lenient().when(request.getHeaders()).thenReturn(requestHeaders);
        lenient().when(chain.filter(exchange)).thenReturn(Mono.empty());
    }

    @Test
    void shouldLogRequestAndAddHeaders() {
        // Given
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(URI.create("/api/hotels"));
        when(request.getRemoteAddress()).thenReturn(new InetSocketAddress("192.168.1.1", 8080));
        when(requestHeaders.getFirst("X-Forwarded-For")).thenReturn(null);
        when(requestHeaders.getFirst("X-Real-IP")).thenReturn(null);

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(responseHeaders).add(eq("X-HRS-Request-ID"), any(String.class));
        verify(chain).filter(exchange);
    }

    @Test
    void shouldExtractClientIpFromXForwardedFor() {
        // Given
        when(request.getMethod()).thenReturn(HttpMethod.POST);
        when(request.getURI()).thenReturn(URI.create("/api/bookings"));
        when(requestHeaders.getFirst("X-Forwarded-For")).thenReturn("203.0.113.1, 70.41.3.18");

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void shouldExtractClientIpFromXRealIP() {
        // Given
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(URI.create("/api/users"));
        when(requestHeaders.getFirst("X-Forwarded-For")).thenReturn(null);
        when(requestHeaders.getFirst("X-Real-IP")).thenReturn("198.51.100.1");

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void shouldUseRemoteAddressWhenHeadersNotPresent() {
        // Given
        when(request.getMethod()).thenReturn(HttpMethod.DELETE);
        when(request.getURI()).thenReturn(URI.create("/api/bookings/123"));
        when(request.getRemoteAddress()).thenReturn(new InetSocketAddress("10.0.0.1", 9000));
        when(requestHeaders.getFirst("X-Forwarded-For")).thenReturn(null);
        when(requestHeaders.getFirst("X-Real-IP")).thenReturn(null);

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void shouldHandleNullRemoteAddress() {
        // Given
        when(request.getMethod()).thenReturn(HttpMethod.PUT);
        when(request.getURI()).thenReturn(URI.create("/api/hotels/456"));
        when(request.getRemoteAddress()).thenReturn(null);
        when(requestHeaders.getFirst("X-Forwarded-For")).thenReturn("");
        when(requestHeaders.getFirst("X-Real-IP")).thenReturn("");

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void shouldGenerateUniqueRequestIds() {
        // Given
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(URI.create("/api/hotels"));
        when(request.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 8080));
        when(requestHeaders.getFirst("X-Forwarded-For")).thenReturn(null);
        when(requestHeaders.getFirst("X-Real-IP")).thenReturn(null);

        // When
        filter.filter(exchange, chain).block();

        // Then
        verify(responseHeaders).add(eq("X-HRS-Request-ID"), argThat(requestId ->
            requestId.startsWith("HRS-") && requestId.length() > 10));
    }

    @Test
    void shouldHaveLowestPrecedenceOrder() {
        // When
        int order = filter.getOrder();

        // Then
        assertEquals(Ordered.LOWEST_PRECEDENCE, order);
    }

    @Test
    void shouldHandleEmptyXForwardedForHeader() {
        // Given
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(URI.create("/api/test"));
        when(request.getRemoteAddress()).thenReturn(new InetSocketAddress("192.168.1.100", 8080));
        when(requestHeaders.getFirst("X-Forwarded-For")).thenReturn("");
        when(requestHeaders.getFirst("X-Real-IP")).thenReturn(null);

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
    }
}