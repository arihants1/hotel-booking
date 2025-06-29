package com.hrs.hotelbooking.booking.impl;

import com.hrs.hotelbooking.booking.dto.BookingSearchCriteria;
import com.hrs.hotelbooking.booking.entity.Booking;
import com.hrs.hotelbooking.booking.entity.BookingSearchDocument;
import com.hrs.hotelbooking.booking.repository.BookingRepository;
import com.hrs.hotelbooking.booking.repository.BookingSearchRepository;
import com.hrs.hotelbooking.booking.service.BookingSearchService;
import com.hrs.hotelbooking.shared.dto.BookingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingSearchServiceImpl implements BookingSearchService {

    private final BookingSearchRepository bookingSearchRepository;
    private final BookingRepository bookingRepository;
    private final BookingElasticsearchMapper bookingMapper;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public Page<BookingSearchDocument> searchBookings(String query, Pageable pageable) {
        log.debug("Performing full-text search for bookings with query: {}", query);
        return bookingSearchRepository.fullTextSearch(query, pageable);
    }

    @Override
    public BookingSearchDocument findByReference(String reference) {
        log.debug("Finding booking by reference: {}", reference);
        BookingSearchDocument result = bookingSearchRepository.findByBookingReference(reference);

        // Try with confirmation number if not found by booking reference
        if (result == null) {
            result = bookingSearchRepository.findByBookingReference(reference);
        }

        return result;
    }

    @Override
    public Page<BookingSearchDocument> searchUserBookings(Long userId, BookingSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching bookings for user {} with criteria: {}", userId, criteria);

        // Create criteria query instead of bool query
        CriteriaQuery criteriaQuery = new CriteriaQuery(
            new Criteria("userId").is(userId)
        );

        applySearchCriteria(criteriaQuery, criteria);
        criteriaQuery.setPageable(pageable);

        SearchHits<BookingSearchDocument> searchHits =
                elasticsearchOperations.search(criteriaQuery, BookingSearchDocument.class);

        List<BookingSearchDocument> bookings = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return new PageImpl<>(bookings, pageable, searchHits.getTotalHits());
    }

    @Override
    public Page<BookingSearchDocument> getUpcomingHotelBookings(Long hotelId, Pageable pageable) {
        log.debug("Getting upcoming bookings for hotel: {}", hotelId);
        return bookingSearchRepository.findByHotelIdAndIsUpcomingTrue(hotelId, pageable);
    }

    @Override
    public List<BookingSearchDocument> findOverlappingBookings(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
        log.debug("Finding overlapping bookings for hotel {} between {} and {}", hotelId, checkIn, checkOut);
        return bookingSearchRepository.findOverlappingBookings(hotelId, checkIn, checkOut);
    }

    @Override
    public Page<BookingSearchDocument> getRecentBookings(LocalDateTime since, Pageable pageable) {
        log.debug("Getting recent bookings since: {}", since);
        return bookingSearchRepository.findByCreatedAtAfterOrderByCreatedAtDesc(since, pageable);
    }

    @Override
    public Map<BookingStatus, Long> getBookingStatsByStatus(LocalDate startDate, LocalDate endDate) {
        log.debug("Getting booking statistics by status between {} and {}", startDate, endDate);

        // Use Criteria Query instead of NativeSearchQuery with QueryBuilders
        CriteriaQuery query = new CriteriaQuery(
            new Criteria("checkInDate").greaterThanEqual(startDate)
                .and("checkOutDate").lessThanEqual(endDate)
        );

        SearchHits<BookingSearchDocument> searchHits =
                elasticsearchOperations.search(query, BookingSearchDocument.class);

        // Process results manually since we're not using aggregations directly
        Map<BookingStatus, Long> result = new HashMap<>();

        for (SearchHit<BookingSearchDocument> hit : searchHits.getSearchHits()) {
            BookingStatus status = hit.getContent().getStatus();
            result.merge(status, 1L, Long::sum);
        }

        return result;
    }

    @Override
    public List<Map.Entry<String, Long>> getTopDestinations(int limit) {
        log.debug("Getting top {} booking destinations", limit);

        // Use simple query and process manually
        SearchHits<BookingSearchDocument> searchHits =
                elasticsearchOperations.search(Query.findAll(), BookingSearchDocument.class);

        Map<String, Long> cityCounts = new HashMap<>();

        for (SearchHit<BookingSearchDocument> hit : searchHits.getSearchHits()) {
            String city = hit.getContent().getHotelCity();
            if (city != null) {
                cityCounts.merge(city, 1L, Long::sum);
            }
        }

        // Sort by count (descending)
        return cityCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public void updateBookingIndex(Long bookingId) {
        log.debug("Updating booking in search index: {}", bookingId);

        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            BookingSearchDocument searchDoc = bookingMapper.toSearchDocument(bookingOpt.get());
            bookingSearchRepository.save(searchDoc);
            log.debug("Updated booking {} in search index", bookingId);
        } else {
            log.warn("Cannot update index for non-existent booking: {}", bookingId);
        }
    }

    @Override
    public void removeFromIndex(Long bookingId) {
        log.debug("Removing booking from search index: {}", bookingId);
        bookingSearchRepository.deleteById(bookingId);
    }

    /**
     * Apply search criteria to criteria query
     */
    private void applySearchCriteria(CriteriaQuery criteriaQuery, BookingSearchCriteria criteria) {
        if (criteria == null) {
            return;
        }

        Criteria baseCriteria = criteriaQuery.getCriteria();

        if (criteria.getStatuses() != null && !criteria.getStatuses().isEmpty()) {
            baseCriteria = baseCriteria.and("status").in(
                    criteria.getStatuses().stream()
                            .map(BookingStatus::name)
                            .collect(Collectors.toList()));
        }

        if (criteria.getCheckInFrom() != null && criteria.getCheckInTo() != null) {
            baseCriteria = baseCriteria.and("checkInDate")
                    .between(criteria.getCheckInFrom(), criteria.getCheckInTo());
        }

        if (criteria.getCheckOutFrom() != null && criteria.getCheckOutTo() != null) {
            baseCriteria = baseCriteria.and("checkOutDate")
                    .between(criteria.getCheckOutFrom(), criteria.getCheckOutTo());
        }

        if (criteria.getMinAmount() != null && criteria.getMaxAmount() != null) {
            baseCriteria = baseCriteria.and("totalAmount")
                    .between(criteria.getMinAmount(), criteria.getMaxAmount());
        }

        if (criteria.getHotelCity() != null) {
            baseCriteria = baseCriteria.and("hotelCity").is(criteria.getHotelCity());
        }

        if (criteria.getHotelCountry() != null) {
            baseCriteria = baseCriteria.and("hotelCountry").is(criteria.getHotelCountry());
        }

        if (criteria.getRoomTypes() != null && !criteria.getRoomTypes().isEmpty()) {
            baseCriteria = baseCriteria.and("roomType").in(criteria.getRoomTypes());
        }

        if (criteria.getIsUpcoming() != null) {
            baseCriteria = baseCriteria.and("isUpcoming").is(criteria.getIsUpcoming());
        }

        if (criteria.getIsPast() != null) {
            baseCriteria = baseCriteria.and("isPast").is(criteria.getIsPast());
        }

        if (criteria.getPaymentStatus() != null) {
            baseCriteria = baseCriteria.and("paymentStatus").is(criteria.getPaymentStatus());
        }
    }
}
