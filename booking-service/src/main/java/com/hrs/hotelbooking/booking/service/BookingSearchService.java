package com.hrs.hotelbooking.booking.service;

import com.hrs.hotelbooking.booking.dto.BookingSearchCriteria;
import com.hrs.hotelbooking.booking.entity.BookingSearchDocument;
import com.hrs.hotelbooking.shared.dto.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for advanced booking search operations using Elasticsearch
 */
public interface BookingSearchService {

    /**
     * Search bookings with full text across multiple fields
     */
    Page<BookingSearchDocument> searchBookings(String query, Pageable pageable);

    /**
     * Find a booking by its reference number or confirmation number
     */
    BookingSearchDocument findByReference(String reference);

    /**
     * Search bookings for a specific user with advanced criteria
     */
    Page<BookingSearchDocument> searchUserBookings(Long userId, BookingSearchCriteria criteria, Pageable pageable);

    /**
     * Search for upcoming bookings at a hotel
     */
    Page<BookingSearchDocument> getUpcomingHotelBookings(Long hotelId, Pageable pageable);

    /**
     * Find overlapping bookings for availability check
     */
    List<BookingSearchDocument> findOverlappingBookings(Long hotelId, LocalDate checkIn, LocalDate checkOut);

    /**
     * Get recent bookings
     */
    Page<BookingSearchDocument> getRecentBookings(LocalDateTime since, Pageable pageable);

    /**
     * Get booking statistics by status
     */
    Map<BookingStatus, Long> getBookingStatsByStatus(LocalDate startDate, LocalDate endDate);

    /**
     * Get top booking destinations
     */
    List<Map.Entry<String, Long>> getTopDestinations(int limit);

    /**
     * Update a booking in the search index
     */
    void updateBookingIndex(Long bookingId);

    /**
     * Remove a booking from the search index
     */
    void removeFromIndex(Long bookingId);
}
