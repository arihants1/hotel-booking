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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * HRS Booking Data Transfer Object
 * Represents booking information for the HRS hotel reservation system
 *
 * @author arihants1
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Hotel booking information for HRS system")
public class BookingDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Unique booking identifier", example = "1")
    private Long id;

    @NotNull(message = "User ID is required")
    @Min(value = 1, message = "User ID must be positive")
    @Schema(description = "User identifier", example = "1")
    private Long userId;

    @NotNull(message = "Hotel ID is required")
    @Min(value = 1, message = "Hotel ID must be positive")
    @Schema(description = "Hotel identifier", example = "1")
    private Long hotelId;

    @NotNull(message = "Check-in date is required")
    @Future(message = "Check-in date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Check-in date", example = "2025-07-15")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Check-out date", example = "2025-07-18")
    private LocalDate checkOutDate;

    @Size(max = 100, message = "Room type cannot exceed 100 characters")
    @Schema(description = "Room type", example = "DELUXE", allowableValues = {"STANDARD", "DELUXE", "SUITE", "EXECUTIVE"})
    private String roomType;

    @NotNull(message = "Number of rooms is required")
    @Min(value = 1, message = "Number of rooms must be at least 1")
    @Max(value = 5, message = "Number of rooms cannot exceed 5")
    @Schema(description = "Number of rooms", example = "2")
    private Integer numberOfRooms;

    @NotNull(message = "Number of guests is required")
    @Min(value = 1, message = "Number of guests must be at least 1")
    @Max(value = 20, message = "Number of guests cannot exceed 20")
    @Schema(description = "Number of guests", example = "4")
    private Integer numberOfGuests;

    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
    @Schema(description = "Total booking amount", example = "899.97")
    private BigDecimal totalAmount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Base amount cannot be negative")
    @Schema(description = "Base amount before taxes and fees", example = "750.00")
    private BigDecimal baseAmount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Taxes amount cannot be negative")
    @Schema(description = "Taxes amount", example = "75.00")
    private BigDecimal taxesAmount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Fees amount cannot be negative")
    @Schema(description = "Service fees amount", example = "37.50")
    private BigDecimal feesAmount;

    @Schema(description = "Booking status", example = "CONFIRMED")
    private BookingStatus status;

    @Size(max = 50, message = "Booking reference cannot exceed 50 characters")
    @Schema(description = "Unique booking reference", example = "HRS_20250627055856_1234")
    private String bookingReference;

    @Size(max = 50, message = "Confirmation number cannot exceed 50 characters")
    @Schema(description = "Booking confirmation number", example = "CONF20250627123456")
    private String confirmationNumber;

    @Size(max = 1000, message = "Special requests cannot exceed 1000 characters")
    @Schema(description = "Special requests from guest", example = "Late check-in, non-smoking room")
    private String specialRequests;

    @Size(max = 255, message = "Guest name cannot exceed 255 characters")
    @Schema(description = "Primary guest name", example = "John Doe")
    private String guestName;

    @Email(message = "Guest email must be valid")
    @Size(max = 255, message = "Guest email cannot exceed 255 characters")
    @Schema(description = "Guest email address", example = "john.doe@example.com")
    private String guestEmail;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Guest phone must be valid")
    @Schema(description = "Guest phone number", example = "+1-555-0123")
    private String guestPhone;

    @Schema(description = "Payment status", example = "PENDING")
    private String paymentStatus;

    @Schema(description = "Payment method", example = "CREDIT_CARD")
    private String paymentMethod;

    @Size(max = 100, message = "Payment reference cannot exceed 100 characters")
    @Schema(description = "Payment reference", example = "PAY_123456")
    private String paymentReference;

    @Size(max = 1000, message = "Cancellation reason cannot exceed 1000 characters")
    @Schema(description = "Cancellation reason", example = "Change of plans")
    private String cancellationReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Cancellation timestamp", example = "2025-07-10 14:30:00")
    private LocalDateTime cancelledAt;

    @Size(max = 50, message = "Cancelled by field cannot exceed 50 characters")
    @Schema(description = "User who cancelled", example = "arihants1")
    private String cancelledBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Check-in timestamp", example = "2025-07-15 15:00:00")
    private LocalDateTime checkedInAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Check-out timestamp", example = "2025-07-18 11:00:00")
    private LocalDateTime checkedOutAt;

    @DecimalMin(value = "0.0", inclusive = true, message = "Discount amount cannot be negative")
    @Schema(description = "Discount amount", example = "50.00")
    private BigDecimal discountAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Booking creation timestamp", example = "2025-06-27 05:58:56")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Last update timestamp", example = "2025-06-27 05:58:56")
    private LocalDateTime updatedAt;

    // Computed fields
    @Schema(description = "Number of nights", example = "3")
    private Integer nights;

    @Schema(description = "Whether booking can be modified", example = "true")
    private Boolean modifiable;

    @Schema(description = "Whether booking can be cancelled", example = "true")
    private Boolean cancellable;

    @Schema(description = "Refund amount if cancelled", example = "450.00")
    private BigDecimal refundAmount;

    // Enrichment fields for API responses
    @Schema(description = "Hotel details")
    private HotelDTO hotel;

    @Schema(description = "User details")
    private UserDTO user;

    // Custom validation methods

    /**
     * Validate check-out date is after check-in date
     */
    @AssertTrue(message = "Check-out date must be after check-in date")
    public boolean isCheckOutAfterCheckIn() {
        if (checkInDate == null || checkOutDate == null) {
            return true;
        }
        return checkOutDate.isAfter(checkInDate);
    }

    /**
     * Validate guest count per room capacity
     */
    @AssertTrue(message = "Too many guests for number of rooms (max 4 per room)")
    public boolean isGuestCountValid() {
        if (numberOfRooms == null || numberOfGuests == null) {
            return true;
        }
        return numberOfGuests <= (numberOfRooms * 4);
    }

    // Helper methods

    /**
     * Get display name for guest
     */
    public String getDisplayGuestName() {
        if (guestName != null && !guestName.trim().isEmpty()) {
            return guestName.trim();
        }
        return "Guest " + (userId != null ? userId : "Unknown");
    }

    /**
     * Check if booking is for today
     */
    public boolean isToday() {
        LocalDate today = LocalDate.now();
        return (checkInDate != null && checkInDate.equals(today)) ||
                (checkOutDate != null && checkOutDate.equals(today));
    }

    /**
     * Check if booking is upcoming
     */
    public boolean isUpcoming() {
        LocalDate today = LocalDate.now();
        return checkInDate != null && checkInDate.isAfter(today);
    }

    /**
     * Check if booking is in the past
     */
    public boolean isPast() {
        LocalDate today = LocalDate.now();
        return checkOutDate != null && checkOutDate.isBefore(today);
    }

    /**
     * Get formatted total amount
     */
    public String getFormattedAmount() {
        if (totalAmount == null) {
            return "N/A";
        }
        return String.format("$%.2f", totalAmount);
    }
}