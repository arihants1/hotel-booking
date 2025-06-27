package com.hrs.hotelbooking.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * HRS Hotel Booking API Gateway
 * Central entry point for all HRS hotel booking system requests
 * Handles routing, load balancing, rate limiting, and cross-cutting concerns
 * Designed to handle 3 million requests per hour
 * 
 * @author arihants1
 */
@SpringBootApplication(scanBasePackages = {"com.hrs.hotelbooking.gateway", "com.hrs.hotelbooking.shared"})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    /**
     * Configure microservice routes with load balancing and filters
     * Routes requests to appropriate backend services based on path patterns
     * Includes circuit breaker, rate limiting, and retry mechanisms
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Hotel Service Routes - Search, details, availability
                .route("hotel-service", r -> r.path("/api/v1/hotels/**")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(exchange -> 
                                            exchange.getRequest().getRemoteAddress() != null ?
                                            reactor.core.publisher.Mono.just(
                                                exchange.getRequest().getRemoteAddress().toString()) :
                                            reactor.core.publisher.Mono.just("unknown")))
                                .circuitBreaker(config -> config
                                        .setName("hotel-service-cb")
                                        .setFallbackUri("forward:/fallback/hotels"))
                                .retry(retryConfig -> retryConfig.setRetries(3))
                                .addRequestHeader("X-Gateway-Timestamp", String.valueOf(System.currentTimeMillis()))
                                .addRequestHeader("X-HRS-Service", "hotel-service"))
                        .uri("http://localhost:8081"))
                
                // Booking Service Routes - CRUD operations for bookings
                .route("booking-service", r -> r.path("/api/v1/bookings/**")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(exchange -> 
                                            exchange.getRequest().getRemoteAddress() != null ?
                                            reactor.core.publisher.Mono.just(
                                                exchange.getRequest().getRemoteAddress().toString()) :
                                            reactor.core.publisher.Mono.just("unknown")))
                                .circuitBreaker(config -> config
                                        .setName("booking-service-cb")
                                        .setFallbackUri("forward:/fallback/bookings"))
                                .retry(retryConfig -> retryConfig.setRetries(3))
                                .addRequestHeader("X-Gateway-Timestamp", String.valueOf(System.currentTimeMillis()))
                                .addRequestHeader("X-HRS-Service", "booking-service"))
                        .uri("http://localhost:8082"))
                
                // User Service Routes - User management operations
                .route("user-service", r -> r.path("/api/v1/users/**")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(exchange -> 
                                            exchange.getRequest().getRemoteAddress() != null ?
                                            reactor.core.publisher.Mono.just(
                                                exchange.getRequest().getRemoteAddress().toString()) :
                                            reactor.core.publisher.Mono.just("unknown")))
                                .circuitBreaker(config -> config
                                        .setName("user-service-cb")
                                        .setFallbackUri("forward:/fallback/users"))
                                .retry(retryConfig -> retryConfig.setRetries(3))
                                .addRequestHeader("X-Gateway-Timestamp", String.valueOf(System.currentTimeMillis()))
                                .addRequestHeader("X-HRS-Service", "user-service"))
                        .uri("http://localhost:8083"))
                
                // Health check routes for all services
                .route("health-check", r -> r.path("/health/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8080/actuator/health"))
                        
                .build();
    }

    /**
     * Configure Redis-based rate limiter for high-throughput scenarios
     * Supports 3M requests per hour with burst capacity
     * Distributed rate limiting across multiple gateway instances
     */
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter redisRateLimiter() {
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(
                1000, // replenishRate - tokens per second (3M per hour = ~833/sec, buffered to 1000)
                2000, // burstCapacity - max tokens in bucket for handling spikes
                1     // requestedTokens - tokens per request
        );
    }

    /**
     * Configure CORS for web applications
     * Allows cross-origin requests from web frontends
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowCredentials(true);
        corsConfig.addAllowedOriginPattern("*");
        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}