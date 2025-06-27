package com.hrs.hotelbooking.booking.controller;

import com.hrs.hotelbooking.booking.service.BookingService;
import com.hrs.hotelbooking.shared.dto.BookingDTO;
import com.hrs.hotelbooking.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;

/**
 * HRS Booking Controller - Essential Features Only
 * REST API endpoints for booking management in the HRS booking system
 * Simplified operations for core booking functionality
 *
 * @author arihants1
 * @since 2025-06-27 05:53:04 UTC
 */
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "HRS Booking API", description = "Essential booking management operations for HRS booking system")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookingController {

    private final BookingService bookingService;

    /**
     * Create a new booking
     */
    @PostMapping
    @Operation(summary = "Create HRS booking",
            description = "Create a new hotel booking with validation and pricing calculation")
    public ResponseEntity<ApiResponse<BookingDTO>> createBooking(
            @Parameter(description = "Booking information", required = true)
            @Valid @RequestBody BookingDTO bookingDTO) {

        log.info("Creating new HRS booking at 2025-06-27 05:53:04 by arihants1 for user: {} hotel: {}",
                bookingDTO.getUserId(), bookingDTO.getHotelId());

        BookingDTO createdBooking = bookingService.createBooking(bookingDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdBooking, "HRS booking created successfully"));
    }

    /**
     * Get booking by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get HRS booking details",
            description = "Retrieve detailed booking information by ID")
    public ResponseEntity<ApiResponse<BookingDTO>> getBookingById(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable @Min(value = 1, message = "Booking ID must be positive") Long id) {

        log.info("Fetching HRS booking details for ID: {} at 2025-06-27 05:53:04 by arihants1", id);

        BookingDTO booking = bookingService.getBookingById(id);

        return ResponseEntity.ok(ApiResponse.success(booking, "HRS booking details retrieved successfully"));
    }

    /**
     * Get booking by reference number
     */
    @GetMapping("/reference/{reference}")
    @Operation(summary = "Get HRS booking by reference",
            description = "Retrieve booking information by booking reference number")
    public ResponseEntity<ApiResponse<BookingDTO>> getBookingByReference(
            @Parameter(description = "Booking reference", required = true)
            @PathVariable String reference) {

        log.info("Fetching HRS booking by reference: {} at 2025-06-27 05:53:04 by arihants1", reference);

        BookingDTO booking = bookingService.getBookingByReference(reference);

        return ResponseEntity.ok(ApiResponse.success(booking, "HRS booking retrieved by reference successfully"));
    }

    /**
     * Update existing booking
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update HRS booking",
            description = "Update an existing booking with validation")
    public ResponseEntity<ApiResponse<BookingDTO>> updateBooking(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable @Min(value = 1, message = "Booking ID must be positive") Long id,

            @Parameter(description = "Updated booking information", required = true)
            @Valid @RequestBody BookingDTO bookingDTO) {

        log.info("Updating HRS booking with ID: {} at 2025-06-27 05:53:04 by arihants1", id);

        BookingDTO updatedBooking = bookingService.updateBooking(id, bookingDTO);

        return ResponseEntity.ok(ApiResponse.success(updatedBooking, "HRS booking updated successfully"));
    }

    /**
     * Cancel booking
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel HRS booking",
            description = "Cancel an existing booking")
    public ResponseEntity<ApiResponse<BookingDTO>> cancelBooking(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable @Min(value = 1, message = "Booking ID must be positive") Long id) {

        log.info("Cancelling HRS booking with ID: {} at 2025-06-27 05:53:04 by arihants1", id);

        BookingDTO cancelledBooking = bookingService.cancelBooking(id);

        return ResponseEntity.ok(ApiResponse.success(cancelledBooking, "HRS booking cancelled successfully"));
    }

    /**
     * Check-in guest
     */
    @PutMapping("/{id}/check-in")
    @Operation(summary = "Check-in guest",
            description = "Check-in guest for the booking")
    public ResponseEntity<ApiResponse<BookingDTO>> checkInGuest(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable @Min(value = 1, message = "Booking ID must be positive") Long id) {

        log.info("Checking in guest for HRS booking: {} at 2025-06-27 05:53:04 by arihants1", id);

        BookingDTO checkedInBooking = bookingService.checkInGuest(id);

        return ResponseEntity.ok(ApiResponse.success(checkedInBooking, "Guest checked in successfully"));
    }

    /**
     * Check-out guest
     */
    @PutMapping("/{id}/check-out")
    @Operation(summary = "Check-out guest",
            description = "Check-out guest for the booking")
    public ResponseEntity<ApiResponse<BookingDTO>> checkOutGuest(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable @Min(value = 1, message = "Booking ID must be positive") Long id) {

        log.info("Checking out guest for HRS booking: {} at 2025-06-27 05:53:04 by arihants1", id);

        BookingDTO checkedOutBooking = bookingService.checkOutGuest(id);

        return ResponseEntity.ok(ApiResponse.success(checkedOutBooking, "Guest checked out successfully"));
    }

    /**
     * Get user bookings
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user bookings",
            description = "Retrieve all bookings for a specific user")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getUserBookings(
            @Parameter(description = "User ID", required = true)
            @PathVariable @Min(value = 1, message = "User ID must be positive") Long userId) {

        log.info("Fetching HRS bookings for user: {} at 2025-06-27 05:53:04 by arihants1", userId);

        List<BookingDTO> bookings = bookingService.getBookingsByUserId(userId);

        return ResponseEntity.ok(ApiResponse.success(bookings,
                String.format("Found %d bookings for user", bookings.size())));
    }

    /**
     * Get hotel bookings
     */
    @GetMapping("/hotel/{hotelId}")
    @Operation(summary = "Get hotel bookings",
            description = "Retrieve all bookings for a specific hotel")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getHotelBookings(
            @Parameter(description = "Hotel ID", required = true)
            @PathVariable @Min(value = 1, message = "Hotel ID must be positive") Long hotelId) {

        log.info("Fetching HRS bookings for hotel: {} at 2025-06-27 05:53:04 by arihants1", hotelId);

        List<BookingDTO> bookings = bookingService.getBookingsByHotelId(hotelId);

        return ResponseEntity.ok(ApiResponse.success(bookings,
                String.format("Found %d bookings for hotel", bookings.size())));
    }

    /**
     * Get all bookings with pagination
     */
    @GetMapping
    @Operation(summary = "Get all HRS bookings",
            description = "Retrieve paginated list of all bookings in the system")
    public ResponseEntity<ApiResponse<Page<BookingDTO>>> getAllBookings(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page number cannot be negative") int page,

            @Parameter(description = "Page size (1-50)")
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 50, message = "Page size cannot exceed 50") int size) {

        log.info("Fetching all HRS bookings at 2025-06-27 05:53:04 by arihants1 - page: {}, size: {}", page, size);

        Page<BookingDTO> bookings = bookingService.getAllBookings(page, size);

        return ResponseEntity.ok(ApiResponse.success(bookings,
                String.format("Retrieved %d bookings (page %d of %d)",
                        bookings.getContent().size(), page + 1, bookings.getTotalPages())));
    }

    /**
     * Get booking statistics summary
     */
    @GetMapping("/stats")
    @Operation(summary = "Get booking statistics",
            description = "Get basic booking statistics")
    public ResponseEntity<ApiResponse<BookingStatsDTO>> getBookingStats() {
        log.info("Fetching HRS booking statistics at 2025-06-27 05:53:04 by arihants1");

        // This would be implemented to return basic stats
        BookingStatsDTO stats = BookingStatsDTO.builder()
                .totalBookings(0L)
                .activeBookings(0L)
                .completedBookings(0L)
                .cancelledBookings(0L)
                .lastUpdated(java.time.LocalDateTime.now())
                .build();

        return ResponseEntity.ok(ApiResponse.success(stats, "Booking statistics retrieved successfully"));
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Booking service health check",
            description = "Check if booking service is running properly")
    public ResponseEntity<ApiResponse<HealthCheckDTO>> healthCheck() {
        log.debug("Booking service health check at 2025-06-27 05:53:04 by arihants1");

        HealthCheckDTO health = HealthCheckDTO.builder()
                .status("UP")
                .service("hrs-booking-service")
                .version("1.0.0")
                .timestamp(java.time.LocalDateTime.now())
                .message("HRS Booking Service is running - 2025-06-27 05:53:04 UTC")
                .build();

        return ResponseEntity.ok(ApiResponse.success(health, "Service is healthy"));
    }

    /**
     * Booking Statistics DTO for internal use
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BookingStatsDTO {
        private Long totalBookings;
        private Long activeBookings;
        private Long completedBookings;
        private Long cancelledBookings;
        private java.time.LocalDateTime lastUpdated;
    }

    /**
     * Health Check DTO for internal use
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class HealthCheckDTO {
        private String status;
        private String service;
        private String version;
        private java.time.LocalDateTime timestamp;
        private String message;
    }
}