package com.hrs.hotelbooking.gateway.fallback;

import com.hrs.hotelbooking.shared.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * HRS Gateway Fallback Controller
 * Provides fallback responses when backend services are unavailable
 * Ensures graceful degradation for the HRS booking system
 * 
 * @author arihants1
 */
@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    /**
     * Fallback for hotel service failures
     * Returns a user-friendly message when hotel service is down
     */
    @GetMapping("/hotels")
    public ResponseEntity<ApiResponse<Object>> hotelServiceFallback() {
        log.warn("HRS Hotel service fallback triggered at {}", LocalDateTime.now());

        ApiResponse<Object> response = ApiResponse.error(
            "HRS Hotel service is temporarily unavailable. Please try again later.",
            "HRS_HOTEL_SERVICE_UNAVAILABLE",
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * Fallback for booking service failures
     * Critical for booking operations - provides specific guidance
     */
    @GetMapping("/bookings")
    @PostMapping("/bookings")
    public ResponseEntity<ApiResponse<Object>> bookingServiceFallback() {
        log.error("HRS Booking service fallback triggered at {}", LocalDateTime.now());

        ApiResponse<Object> response = ApiResponse.error(
            "HRS Booking service is currently experiencing issues. Your booking request could not be processed. Please contact HRS support if this persists.",
            "HRS_BOOKING_SERVICE_UNAVAILABLE",
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * Fallback for user service failures
     * Handles user management operations
     */
    @GetMapping("/users")
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<Object>> userServiceFallback() {
        log.warn("HRS User service fallback triggered at {}", LocalDateTime.now());

        ApiResponse<Object> response = ApiResponse.error(
            "HRS User service is temporarily unavailable. Please try again later.",
            "HRS_USER_SERVICE_UNAVAILABLE",
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * General fallback for any unhandled service failures
     */
    @GetMapping("/general")
    public ResponseEntity<ApiResponse<Object>> generalFallback() {
        log.error("HRS General fallback triggered at {}", LocalDateTime.now());

        ApiResponse<Object> response = ApiResponse.error(
            "HRS system is experiencing technical difficulties. Please try again later or contact support.",
            "HRS_GENERAL_SERVICE_UNAVAILABLE",
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}