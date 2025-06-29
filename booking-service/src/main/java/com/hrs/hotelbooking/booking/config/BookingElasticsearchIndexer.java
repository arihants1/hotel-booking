package com.hrs.hotelbooking.booking.config;

import com.hrs.hotelbooking.booking.entity.Booking;
import com.hrs.hotelbooking.booking.entity.BookingSearchDocument;
import com.hrs.hotelbooking.booking.repository.BookingRepository;
import com.hrs.hotelbooking.booking.repository.BookingSearchRepository;
import com.hrs.hotelbooking.shared.dto.BookingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Indexer to synchronize booking data from PostgreSQL to Elasticsearch on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingElasticsearchIndexer implements CommandLineRunner {

    private final BookingRepository bookingRepository;
    private final BookingSearchRepository bookingSearchRepository;

    @Value("${hrs.elasticsearch.batch-size:100}")
    private int batchSize;

    @Override
    public void run(String... args) {
        log.info("Starting Elasticsearch indexer - synchronizing bookings from PostgreSQL to Elasticsearch...");

        int newCount = 0;
        int updatedCount = 0;
        int pageNumber = 0;
        int totalProcessed = 0;
        long start = System.currentTimeMillis();

        // Get total count for progress tracking
        long totalCount = bookingRepository.count();
        log.info("Found {} total bookings in database to process", totalCount);

        if (totalCount == 0) {
            log.warn("No bookings found in database. Elasticsearch index will be empty.");
            return;
        }

        // Process bookings in batches using pagination
        Page<Booking> bookingPage;
        do {
            Pageable pageable = PageRequest.of(pageNumber, batchSize);
            bookingPage = bookingRepository.findAll(pageable);

            List<BookingSearchDocument> batch = new ArrayList<>();

            for (Booking booking : bookingPage.getContent()) {
                boolean exists = bookingSearchRepository.existsById(booking.getId());

                BookingSearchDocument doc = mapToSearchDocument(booking);
                batch.add(doc);

                if (exists) {
                    updatedCount++;
                } else {
                    newCount++;
                }
            }

            // Bulk save the batch
            if (!batch.isEmpty()) {
                bookingSearchRepository.saveAll(batch);
            }

            totalProcessed += bookingPage.getNumberOfElements();
            log.info("Processed {}/{} bookings ({}%)",
                    totalProcessed,
                    totalCount,
                    Math.round((double) totalProcessed / totalCount * 100));

            pageNumber++;
        } while (bookingPage.hasNext());

        long duration = System.currentTimeMillis() - start;
        log.info("Elasticsearch indexing complete: {} new bookings indexed, {} existing bookings updated in {}ms",
                newCount, updatedCount, duration);
    }

    /**
     * Maps a database booking entity to an Elasticsearch document
     */
    private BookingSearchDocument mapToSearchDocument(Booking booking) {
        // Calculate derived fields
        Integer stayDuration = null;
        if (booking.getCheckInDate() != null && booking.getCheckOutDate() != null) {
            stayDuration = (int) ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        }

        boolean isActive = booking.getStatus() == BookingStatus.CONFIRMED ||
                           booking.getStatus() == BookingStatus.CHECKED_IN;

        boolean isUpcoming = booking.getCheckInDate() != null && booking.getCheckInDate().isAfter(LocalDate.now());
        boolean isPast = booking.getCheckOutDate() != null && booking.getCheckOutDate().isBefore(LocalDate.now());

        // Generate tags for faceted search
        List<String> tags = new ArrayList<>();
        if (booking.getNumberOfGuests() > 2) tags.add("group");
        if (booking.getNumberOfRooms() > 1) tags.add("multi-room");
        if (stayDuration != null && stayDuration > 7) tags.add("long-stay");
        if (booking.getStatus() == BookingStatus.CANCELLED) tags.add("cancelled");
        if (booking.getStatus() == BookingStatus.CHECKED_IN) tags.add("active-stay");

        // Build searchable text for full-text search
        StringBuilder searchText = new StringBuilder();
        if (booking.getGuestName() != null) searchText.append(booking.getGuestName()).append(" ");
        if (booking.getBookingReference() != null) searchText.append(booking.getBookingReference()).append(" ");
        if (booking.getConfirmationNumber() != null) searchText.append(booking.getConfirmationNumber()).append(" ");
        if (booking.getSpecialRequests() != null) searchText.append(booking.getSpecialRequests());

        return BookingSearchDocument.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .hotelId(booking.getHotelId())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .roomType(booking.getRoomType())
                .numberOfRooms(booking.getNumberOfRooms())
                .numberOfGuests(booking.getNumberOfGuests())
                .totalAmount(booking.getTotalAmount())
                .baseAmount(booking.getBaseAmount())
                .taxesAmount(booking.getTaxesAmount())
                .feesAmount(booking.getFeesAmount())
                .status(booking.getStatus())
                .bookingReference(booking.getBookingReference())
                .confirmationNumber(booking.getConfirmationNumber())
                .specialRequests(booking.getSpecialRequests())
                .guestName(booking.getGuestName())
                .guestEmail(booking.getGuestEmail())
                .guestPhone(booking.getGuestPhone())
                .paymentStatus(booking.getPaymentStatus())
                .paymentMethod(booking.getPaymentMethod())
                .cancelledAt(booking.getCancelledAt())
                .checkedInAt(booking.getCheckedInAt())
                .checkedOutAt(booking.getCheckedOutAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                // Derived fields
                .stayDuration(stayDuration)
                .isActive(isActive)
                .isUpcoming(isUpcoming)
                .isPast(isPast)
                .tags(tags)
                .searchableText(searchText.toString())
                .build();
    }
}
