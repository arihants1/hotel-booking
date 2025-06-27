package com.hrs.hotelbooking.booking.entity;

import com.hrs.hotelbooking.shared.dto.BookingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * HRS Booking Entity - Essential Fields Only
 * Represents booking information in the HRS booking system database
 * Simplified for core booking operations
 *
 * @author arihants1
 * @since 2025-06-27 05:47:04 UTC
 */
@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_user", columnList = "user_id"),
        @Index(name = "idx_booking_hotel", columnList = "hotel_id"),
        @Index(name = "idx_booking_dates", columnList = "check_in_date, check_out_date"),
        @Index(name = "idx_booking_status", columnList = "status"),
        @Index(name = "idx_booking_reference", columnList = "booking_reference"),
        @Index(name = "idx_booking_created", columnList = "created_at"),
        @Index(name = "idx_booking_user_status", columnList = "user_id, status"),
        @Index(name = "idx_booking_hotel_dates", columnList = "hotel_id, check_in_date, check_out_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "room_type", length = 100)
    private String roomType;

    @Column(name = "number_of_rooms", nullable = false)
    @Builder.Default
    private Integer numberOfRooms = 1;

    @Column(name = "number_of_guests", nullable = false)
    @Builder.Default
    private Integer numberOfGuests = 1;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "base_amount", precision = 12, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "taxes_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxesAmount = BigDecimal.ZERO;

    @Column(name = "fees_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal feesAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(name = "booking_reference", length = 50, unique = true, nullable = false)
    private String bookingReference;

    @Column(name = "confirmation_number", length = 50, unique = true)
    private String confirmationNumber;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "guest_name", length = 255)
    private String guestName;

    @Column(name = "guest_email", length = 255)
    private String guestEmail;

    @Column(name = "guest_phone", length = 50)
    private String guestPhone;

    @Column(name = "payment_status", length = 50)
    @Builder.Default
    private String paymentStatus = "PENDING";

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by", length = 50)
    private String cancelledBy;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Column(name = "checked_out_at")
    private LocalDateTime checkedOutAt;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 50)
    @Builder.Default
    private String createdBy = "arihants1";

    @Column(name = "updated_by", length = 50)
    @Builder.Default
    private String updatedBy = "arihants1";

    @Column(name = "version")
    @Version
    @Builder.Default
    private Long version = 0L;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (createdBy == null) {
            createdBy = "arihants1";
        }
        updatedBy = createdBy;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        updatedBy = "arihants1";
    }

    /**
     * Calculate total number of nights
     */
    public long calculateNights() {
        if (checkInDate != null && checkOutDate != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        }
        return 0;
    }

    /**
     * Check if booking is modifiable
     */
    public boolean isModifiable() {
        return status == BookingStatus.CONFIRMED || status == BookingStatus.PENDING;
    }

    /**
     * Check if booking is cancellable
     */
    public boolean isCancellable() {
        return status == BookingStatus.CONFIRMED || status == BookingStatus.PENDING;
    }

    /**
     * Check if booking is in past
     */
    public boolean isPastBooking() {
        return checkOutDate != null && checkOutDate.isBefore(LocalDate.now());
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
     * Calculate refund amount based on cancellation policy
     */
    public BigDecimal calculateRefundAmount() {
        if (status != BookingStatus.CANCELLED || totalAmount == null) {
            return BigDecimal.ZERO;
        }

        // Simple cancellation policy - full refund if cancelled 24 hours before check-in
        LocalDate today = LocalDate.now();
        if (checkInDate != null && checkInDate.isAfter(today.plusDays(1))) {
            return totalAmount;
        }

        // 50% refund if cancelled same day
        if (checkInDate != null && checkInDate.equals(today)) {
            return totalAmount.multiply(new BigDecimal("0.5"));
        }

        // No refund for past dates
        return BigDecimal.ZERO;
    }
}