package com.hrs.hotelbooking.booking.controller;

import com.hrs.hotelbooking.booking.dto.BookingSearchCriteria;
import com.hrs.hotelbooking.booking.entity.BookingSearchDocument;
import com.hrs.hotelbooking.booking.service.BookingSearchService;
import com.hrs.hotelbooking.shared.dto.BookingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST controller for booking search operations
 */
@RestController
@RequestMapping("/api/v1/bookings/search")
@RequiredArgsConstructor
@Slf4j
public class BookingSearchController {

    private final BookingSearchService bookingSearchService;

    @GetMapping
    public ResponseEntity<Page<BookingSearchDocument>> searchBookings(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("Searching bookings with query: {}, page: {}, size: {}", query, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BookingSearchDocument> results = bookingSearchService.searchBookings(query, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<BookingSearchDocument> findByReference(@PathVariable String reference) {
        log.debug("Finding booking by reference: {}", reference);
        BookingSearchDocument booking = bookingSearchService.findByReference(reference);
        return booking != null ? ResponseEntity.ok(booking) : ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<BookingSearchDocument>> searchUserBookings(
            @PathVariable Long userId,
            @RequestParam(required = false) List<BookingStatus> statuses,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("Searching bookings for user: {}", userId);

        BookingSearchCriteria criteria = BookingSearchCriteria.builder()
                .statuses(statuses)
                .checkInFrom(checkInFrom)
                .checkInTo(checkInTo)
                .checkOutFrom(checkOutFrom)
                .checkOutTo(checkOutTo)
                .isUpcoming(checkInFrom == null && checkInTo == null ? true : null)
                .build();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BookingSearchDocument> results = bookingSearchService.searchUserBookings(userId, criteria, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/hotel/{hotelId}/upcoming")
    public ResponseEntity<Page<BookingSearchDocument>> getUpcomingHotelBookings(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("Getting upcoming bookings for hotel: {}", hotelId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("checkInDate").ascending());
        Page<BookingSearchDocument> results = bookingSearchService.getUpcomingHotelBookings(hotelId, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/stats/status")
    public ResponseEntity<Map<BookingStatus, Long>> getBookingStatsByStatus(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.debug("Getting booking statistics by status between {} and {}", startDate, endDate);
        Map<BookingStatus, Long> stats = bookingSearchService.getBookingStatsByStatus(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/destinations")
    public ResponseEntity<List<Map.Entry<String, Long>>> getTopDestinations(
            @RequestParam(defaultValue = "10") int limit) {

        log.debug("Getting top {} booking destinations", limit);
        List<Map.Entry<String, Long>> destinations = bookingSearchService.getTopDestinations(limit);
        return ResponseEntity.ok(destinations);
    }

    @GetMapping("/recent")
    public ResponseEntity<Page<BookingSearchDocument>> getRecentBookings(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("Getting recent bookings since: {}", since);

        // Default to last 24 hours if not specified
        if (since == null) {
            since = LocalDateTime.now().minusDays(1);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BookingSearchDocument> results = bookingSearchService.getRecentBookings(since, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/hotel/{hotelId}/overlapping")
    public ResponseEntity<List<BookingSearchDocument>> findOverlappingBookings(
            @PathVariable Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {

        log.debug("Finding overlapping bookings for hotel {} between {} and {}", hotelId, checkIn, checkOut);
        List<BookingSearchDocument> results = bookingSearchService.findOverlappingBookings(hotelId, checkIn, checkOut);
        return ResponseEntity.ok(results);
    }
}
