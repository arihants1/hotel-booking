package com.hrs.hotelbooking.booking.repository;

import com.hrs.hotelbooking.booking.entity.BookingSearchDocument;
import com.hrs.hotelbooking.shared.dto.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for searching bookings using Elasticsearch
 */
@Repository
public interface BookingSearchRepository extends ElasticsearchRepository<BookingSearchDocument, Long> {

    BookingSearchDocument findByBookingReference(String bookingReference);

    // For availability checks
    @Query("{\"bool\": {\"must\": [{\"term\": {\"hotelId\": ?0}}, {\"range\": {\"checkInDate\": {\"lte\": \"?2\"}}}, {\"range\": {\"checkOutDate\": {\"gte\": \"?1\"}}}]}}")
    List<BookingSearchDocument> findOverlappingBookings(Long hotelId, LocalDate checkInDate, LocalDate checkOutDate);

    // Advanced full-text search
    @Query("{\"bool\": {\"should\": [{\"match\": {\"guestName\": \"?0\"}}, {\"match\": {\"bookingReference\": \"?0\"}}, {\"match\": {\"confirmationNumber\": \"?0\"}}, {\"match\": {\"searchableText\": \"?0\"}}]}}")
    Page<BookingSearchDocument> fullTextSearch(String searchTerm, Pageable pageable);


    Page<BookingSearchDocument> findByHotelIdAndIsUpcomingTrue(Long hotelId, Pageable pageable);

    // Recent bookings
    Page<BookingSearchDocument> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime dateTime, Pageable pageable);

}
