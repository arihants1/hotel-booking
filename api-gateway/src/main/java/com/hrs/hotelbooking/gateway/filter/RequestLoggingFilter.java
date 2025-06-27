package com.hrs.hotelbooking.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * HRS Request Logging Filter
 * Logs all incoming requests for monitoring and debugging
 * Tracks request patterns for the HRS booking system
 * 
 * @author arihants1
 * @since 2025-06-26 19:23:24 UTC
 */
@Component
@Slf4j
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Log request details for HRS monitoring
        log.info("HRS Gateway Request: {} {} from {} at {}", 
                request.getMethod(), 
                request.getURI().getPath(), 
                getClientIp(request),
                LocalDateTime.now());
        
        // Add request ID for tracing
        String requestId = "HRS-" + System.currentTimeMillis() + "-" + 
                          Math.abs(request.getURI().getPath().hashCode() % 10000);
        
        exchange.getResponse().getHeaders().add("X-HRS-Request-ID", requestId);
        
        long startTime = System.currentTimeMillis();
        
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            log.info("HRS Gateway Response: {} {} completed in {}ms (Request ID: {})", 
                    request.getMethod(), 
                    request.getURI().getPath(), 
                    duration,
                    requestId);
        }));
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null ? 
               request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}