package com.hrs.hotelbooking.booking.repository;

import com.hrs.hotelbooking.booking.entity.Booking;
import com.hrs.hotelbooking.shared.dto.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * HRS Booking Repository - Essential Operations Only
 * Data access for booking operations in the HRS booking system
 * Simplified queries for core booking functionality
 *
 * @author arihants1
 * @since 2025-06-27 05:47:04 UTC
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Find booking by reference number
     */
    Optional<Booking> findByBookingReference(String bookingReference);

    /**
     * Check if booking reference exists
     */
    boolean existsByBookingReference(String bookingReference);

    /**
     * Check if confirmation number exists
     */
    boolean existsByConfirmationNumber(String confirmationNumber);

    /**
     * Find bookings by user ID ordered by creation date
     */
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find bookings by hotel ID ordered by check-in date
     */
    List<Booking> findByHotelIdOrderByCheckInDateAsc(Long hotelId);


    /**
     * Find duplicate bookings (same user, hotel, overlapping dates)
     */
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId AND b.hotelId = :hotelId AND " +
            "b.status IN ('CONFIRMED', 'CHECKED_IN', 'PENDING') AND " +
            "((b.checkInDate <= :checkOut AND b.checkOutDate >= :checkIn)) AND " +
            "(:excludeId IS NULL OR b.id != :excludeId)")
    List<Booking> findDuplicateBookings(@Param("userId") Long userId,
                                        @Param("hotelId") Long hotelId,
                                        @Param("checkIn") LocalDate checkIn,
                                        @Param("checkOut") LocalDate checkOut,
                                        @Param("excludeId") Long excludeId);

}