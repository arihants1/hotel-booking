package com.hrs.hotelbooking.booking.impl;

import com.hrs.hotelbooking.booking.entity.Booking;
import com.hrs.hotelbooking.booking.entity.BookingSearchDocument;
import com.hrs.hotelbooking.shared.dto.BookingStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for converting between Booking entities and Elasticsearch documents
 */
@Component
public class BookingElasticsearchMapper {

    /**
     * Convert a booking entity to an Elasticsearch document
     */
    public BookingSearchDocument toSearchDocument(Booking booking) {
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
                .stayDuration(stayDuration)
                .isActive(isActive)
                .isUpcoming(isUpcoming)
                .isPast(isPast)
                .tags(tags)
                .searchableText(searchText.toString())
                .build();
    }
}
