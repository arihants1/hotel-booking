package com.hrs.hotelbooking.hotel.controller;

import com.hrs.hotelbooking.hotel.service.HotelService;
import com.hrs.hotelbooking.shared.dto.HotelDTO;
import com.hrs.hotelbooking.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDate;
import java.util.List;

/**
 * HRS Hotel Controller
 * REST API endpoints for hotel management in the HRS booking system
 * Simplified operations for core hotel functionality
 *
 * @author arihants1
 */
@RestController
@RequestMapping("/api/v1/hotels")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "HRS Hotel API", description = "Essential hotel management operations for HRS booking system")
public class HotelController {

    private final HotelService hotelService;

    /**
     * Search hotels with basic criteria
     */
    @GetMapping("/search")
    @Operation(summary = "Search HRS hotels",
            description = "Search hotels by city and date range with basic filters")
    public ResponseEntity<ApiResponse<List<HotelDTO>>> searchHotels(
            @Parameter(description = "City name (optional)")
            @RequestParam(required = false) String city,

            @Parameter(description = "Check-in date (optional)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,

            @Parameter(description = "Check-out date (optional)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,

            @Parameter(description = "Number of rooms (optional)")
            @RequestParam(required = false) Integer rooms,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page number cannot be negative") int page,

            @Parameter(description = "Page size (1-50)")
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 50, message = "Page size cannot exceed 50") int size) {

        log.info("Searching HRS hotels: city={}, checkIn={}, checkOut={}, rooms={}",
                city, checkIn, checkOut, rooms);

        List<HotelDTO> hotels = hotelService.searchHotels(city, checkIn, checkOut, rooms, page, size);

        return ResponseEntity.ok(ApiResponse.success(hotels,
                String.format("Found %d hotels", hotels.size())));
    }

    /**
     * Get hotel by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get HRS hotel details",
            description = "Retrieve detailed hotel information by ID")
    public ResponseEntity<ApiResponse<HotelDTO>> getHotelById(
            @Parameter(description = "Hotel ID", required = true)
            @PathVariable @Min(value = 1, message = "Hotel ID must be positive") Long id) {

        log.info("Fetching HRS hotel details for ID: {} ", id);

        HotelDTO hotel = hotelService.getHotelById(id);

        return ResponseEntity.ok(ApiResponse.success(hotel, "Hotel details retrieved successfully"));
    }

    /**
     * Get all hotels with pagination
     */
    @GetMapping
    @Operation(summary = "Get all HRS hotels",
            description = "Retrieve paginated list of all active hotels")
    public ResponseEntity<ApiResponse<Page<HotelDTO>>> getAllHotels(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page number cannot be negative") int page,

            @Parameter(description = "Page size (1-50)")
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 50, message = "Page size cannot exceed 50") int size) {

        log.info("Fetching all HRS hotels - page: {}, size: {}", page, size);

        Page<HotelDTO> hotels = hotelService.getAllHotels(page, size);

        return ResponseEntity.ok(ApiResponse.success(hotels,
                String.format("Retrieved %d hotels (page %d of %d)",
                        hotels.getContent().size(), page + 1, hotels.getTotalPages())));
    }

    /**
     * Get hotels by city
     */
    @GetMapping("/city/{city}")
    @Operation(summary = "Get hotels by city",
            description = "Retrieve all hotels in a specific city")
    public ResponseEntity<ApiResponse<List<HotelDTO>>> getHotelsByCity(
            @Parameter(description = "City name", required = true)
            @PathVariable String city) {

        log.info("Fetching HRS hotels in city: {} by arihants1", city);

        List<HotelDTO> hotels = hotelService.getHotelsByCity(city);

        return ResponseEntity.ok(ApiResponse.success(hotels,
                String.format("Found %d hotels in %s", hotels.size(), city)));
    }

    /**
     * Create new hotel
     */
    @PostMapping
    @Operation(summary = "Create HRS hotel",
            description = "Create a new hotel with validation")
    public ResponseEntity<ApiResponse<HotelDTO>> createHotel(
            @Parameter(description = "Hotel information", required = true)
            @Valid @RequestBody HotelDTO hotelDTO) {

        log.info("Creating new HRS hotel: {} ", hotelDTO.getName());

        HotelDTO createdHotel = hotelService.createHotel(hotelDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdHotel, "Hotel created successfully"));
    }

    /**
     * Update existing hotel
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update HRS hotel",
            description = "Update an existing hotel with validation")
    public ResponseEntity<ApiResponse<HotelDTO>> updateHotel(
            @Parameter(description = "Hotel ID", required = true)
            @PathVariable @Min(value = 1, message = "Hotel ID must be positive") Long id,

            @Parameter(description = "Updated hotel information", required = true)
            @Valid @RequestBody HotelDTO hotelDTO) {

        log.info("Updating HRS hotel with ID: {} ", id);

        HotelDTO updatedHotel = hotelService.updateHotel(id, hotelDTO);

        return ResponseEntity.ok(ApiResponse.success(updatedHotel, "Hotel updated successfully"));
    }

    /**
     * Delete hotel (soft delete)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete HRS hotel",
            description = "Soft delete a hotel (marks as inactive)")
    public ResponseEntity<ApiResponse<Void>> deleteHotel(
            @Parameter(description = "Hotel ID", required = true)
            @PathVariable @Min(value = 1, message = "Hotel ID must be positive") Long id) {

        log.info("Deleting HRS hotel with ID: {} ", id);

        hotelService.deleteHotel(id);

        return ResponseEntity.ok(ApiResponse.success(null, "Hotel deleted successfully"));
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Hotel service health check",
            description = "Check if hotel service is running properly")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        log.debug("Hotel service health check ");

        return ResponseEntity.ok(ApiResponse.success("OK",
                "HRS Hotel Service is running"));
    }
}