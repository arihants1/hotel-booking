package com.hrs.hotelbooking.booking.service;

import com.hrs.hotelbooking.shared.dto.BookingDTO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * HRS Booking Service Interface - Essential Features Only
 * Core booking operations for the HRS booking system
 * Simplified contract for booking management
 *
 * @author arihants1
 */
public interface BookingService {

    /**
     * Create a new booking with validation
     */
    BookingDTO createBooking(BookingDTO bookingDTO);

    /**
     * Get booking by ID
     */
    BookingDTO getBookingById(Long id);

    /**
     * Get booking by reference number
     */
    BookingDTO getBookingByReference(String bookingReference);

    /**
     * Update existing booking
     */
    BookingDTO updateBooking(Long id, BookingDTO bookingDTO);

    /**
     * Cancel booking
     */
    BookingDTO cancelBooking(Long id);

    /**
     * Get all bookings by user ID
     */
    List<BookingDTO> getBookingsByUserId(Long userId);

    /**
     * Get all bookings by hotel ID
     */
    List<BookingDTO> getBookingsByHotelId(Long hotelId);

    /**
     * Check-in guest
     */
    BookingDTO checkInGuest(Long bookingId);

    /**
     * Check-out guest
     */
    BookingDTO checkOutGuest(Long bookingId);

    /**
     * Get all bookings with pagination
     */
    Page<BookingDTO> getAllBookings(int page, int size);
}