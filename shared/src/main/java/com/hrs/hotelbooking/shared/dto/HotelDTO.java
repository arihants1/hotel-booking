package com.hrs.hotelbooking.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * HRS Hotel Data Transfer Object
 *
 * @author arihants1
 * @since 2025-06-27 05:37:49 UTC
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Hotel information for HRS system")
public class HotelDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Hotel ID", example = "1")
    private Long id;

    @NotBlank(message = "Hotel name is required")
    @Size(max = 255, message = "Hotel name cannot exceed 255 characters")
    @Schema(description = "Hotel name", example = "HRS Grand Hotel")
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Schema(description = "Hotel description", example = "Luxury hotel in city center")
    private String description;

    @NotBlank(message = "Location is required")
    @Size(max = 500, message = "Location cannot exceed 500 characters")
    @Schema(description = "Hotel location", example = "123 Main Street, Downtown")
    private String location;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    @Schema(description = "City", example = "New York")
    private String city;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    @Schema(description = "Country", example = "USA")
    private String country;

    @Min(value = 1, message = "Star rating must be at least 1")
    @Max(value = 5, message = "Star rating cannot exceed 5")
    @Schema(description = "Star rating", example = "4")
    private Integer starRating;

    @Schema(description = "Hotel amenities")
    private Map<String, Object> amenities;

    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than 0")
    @Schema(description = "Base price per night", example = "199.99")
    private BigDecimal basePrice;

    @Min(value = 1, message = "Total rooms must be at least 1")
    @Schema(description = "Total number of rooms", example = "150")
    private Integer totalRooms;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone must be valid")
    @Schema(description = "Phone number", example = "+1-555-0123")
    private String phone;

    @Email(message = "Email must be valid")
    @Schema(description = "Email address", example = "info@hrsgrand.com")
    private String email;

    @Schema(description = "Website URL", example = "https://hrsgrand.com")
    private String website;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Creation timestamp", example = "2025-06-27 05:37:49")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Last update timestamp", example = "2025-06-27 05:37:49")
    private LocalDateTime updatedAt;

    // Computed fields
    @Schema(description = "Available rooms for search period")
    private Integer availableRooms;
}