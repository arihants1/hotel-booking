package com.hrs.hotelbooking.booking.impl;

import com.hrs.hotelbooking.booking.entity.Booking;
import com.hrs.hotelbooking.booking.entity.BookingSearchDocument;
import com.hrs.hotelbooking.shared.dto.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test cases for BookingElasticsearchMapper
 * Tests the mapping between Booking entities and BookingSearchDocument
 *
 * @author arihants1
 */
@DisplayName("Booking Elasticsearch Mapper Tests")
class BookingElasticsearchMapperTest {

    private BookingElasticsearchMapper mapper;
    private Booking baseBooking;

    @BeforeEach
    void setUp() {
        mapper = new BookingElasticsearchMapper();

        // Create a base booking with all fields populated
        baseBooking = Booking.builder()
                .id(1L)
                .userId(101L)
                .hotelId(201L)
                .checkInDate(LocalDate.of(2025, 8, 1))
                .checkOutDate(LocalDate.of(2025, 8, 5))
                .roomType("Deluxe Suite")
                .numberOfRooms(2)
                .numberOfGuests(4)
                .totalAmount(new BigDecimal("899.99"))
                .baseAmount(new BigDecimal("750.00"))
                .taxesAmount(new BigDecimal("112.50"))
                .feesAmount(new BigDecimal("37.49"))
                .status(BookingStatus.CONFIRMED)
                .bookingReference("HRS202508010001")
                .confirmationNumber("CNF123456789")
                .specialRequests("Late check-in requested, high floor preferred")
                .guestName("John Smith")
                .guestEmail("john.smith@example.com")
                .guestPhone("+1-555-123-4567")
                .paymentStatus("PAID")
                .paymentMethod("CREDIT_CARD")
                .createdAt(LocalDateTime.of(2025, 7, 15, 10, 30))
                .updatedAt(LocalDateTime.of(2025, 7, 16, 14, 20))
                .build();
    }

    /**
     * Helper method to create a booking with modified fields
     */
    private Booking createBookingWith(java.util.function.Consumer<Booking.BookingBuilder> modifications) {
        Booking.BookingBuilder builder = Booking.builder()
                .id(baseBooking.getId())
                .userId(baseBooking.getUserId())
                .hotelId(baseBooking.getHotelId())
                .checkInDate(baseBooking.getCheckInDate())
                .checkOutDate(baseBooking.getCheckOutDate())
                .roomType(baseBooking.getRoomType())
                .numberOfRooms(baseBooking.getNumberOfRooms())
                .numberOfGuests(baseBooking.getNumberOfGuests())
                .totalAmount(baseBooking.getTotalAmount())
                .baseAmount(baseBooking.getBaseAmount())
                .taxesAmount(baseBooking.getTaxesAmount())
                .feesAmount(baseBooking.getFeesAmount())
                .status(baseBooking.getStatus())
                .bookingReference(baseBooking.getBookingReference())
                .confirmationNumber(baseBooking.getConfirmationNumber())
                .specialRequests(baseBooking.getSpecialRequests())
                .guestName(baseBooking.getGuestName())
                .guestEmail(baseBooking.getGuestEmail())
                .guestPhone(baseBooking.getGuestPhone())
                .paymentStatus(baseBooking.getPaymentStatus())
                .paymentMethod(baseBooking.getPaymentMethod())
                .createdAt(baseBooking.getCreatedAt())
                .updatedAt(baseBooking.getUpdatedAt());

        modifications.accept(builder);
        return builder.build();
    }

    @Test
    @DisplayName("Should map all basic booking fields correctly")
    void shouldMapAllBasicBookingFieldsCorrectly() {
        // When
        BookingSearchDocument document = mapper.toSearchDocument(baseBooking);

        // Then
        assertThat(document.getId()).isEqualTo(baseBooking.getId());
        assertThat(document.getUserId()).isEqualTo(baseBooking.getUserId());
        assertThat(document.getHotelId()).isEqualTo(baseBooking.getHotelId());
        assertThat(document.getCheckInDate()).isEqualTo(baseBooking.getCheckInDate());
        assertThat(document.getCheckOutDate()).isEqualTo(baseBooking.getCheckOutDate());
        assertThat(document.getRoomType()).isEqualTo(baseBooking.getRoomType());
        assertThat(document.getNumberOfRooms()).isEqualTo(baseBooking.getNumberOfRooms());
        assertThat(document.getNumberOfGuests()).isEqualTo(baseBooking.getNumberOfGuests());
        assertThat(document.getTotalAmount()).isEqualTo(baseBooking.getTotalAmount());
        assertThat(document.getBaseAmount()).isEqualTo(baseBooking.getBaseAmount());
        assertThat(document.getTaxesAmount()).isEqualTo(baseBooking.getTaxesAmount());
        assertThat(document.getFeesAmount()).isEqualTo(baseBooking.getFeesAmount());
        assertThat(document.getStatus()).isEqualTo(baseBooking.getStatus());
        assertThat(document.getBookingReference()).isEqualTo(baseBooking.getBookingReference());
        assertThat(document.getConfirmationNumber()).isEqualTo(baseBooking.getConfirmationNumber());
        assertThat(document.getSpecialRequests()).isEqualTo(baseBooking.getSpecialRequests());
        assertThat(document.getGuestName()).isEqualTo(baseBooking.getGuestName());
        assertThat(document.getGuestEmail()).isEqualTo(baseBooking.getGuestEmail());
        assertThat(document.getGuestPhone()).isEqualTo(baseBooking.getGuestPhone());
        assertThat(document.getPaymentStatus()).isEqualTo(baseBooking.getPaymentStatus());
        assertThat(document.getPaymentMethod()).isEqualTo(baseBooking.getPaymentMethod());
        assertThat(document.getCreatedAt()).isEqualTo(baseBooking.getCreatedAt());
        assertThat(document.getUpdatedAt()).isEqualTo(baseBooking.getUpdatedAt());
    }

    @Test
    @DisplayName("Should calculate stay duration correctly")
    void shouldCalculateStayDurationCorrectly() {
        // Given - 4 days stay (Aug 1-5)
        Booking booking = createBookingWith(builder -> builder
                .checkInDate(LocalDate.of(2025, 8, 1))
                .checkOutDate(LocalDate.of(2025, 8, 5))
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getStayDuration()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should handle null check-in date for stay duration")
    void shouldHandleNullCheckInDateForStayDuration() {
        // Given
        Booking booking = createBookingWith(builder -> builder
                .checkInDate(null)
                .checkOutDate(LocalDate.of(2025, 8, 5))
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getStayDuration()).isNull();
    }

    @Test
    @DisplayName("Should handle null check-out date for stay duration")
    void shouldHandleNullCheckOutDateForStayDuration() {
        // Given
        Booking booking = createBookingWith(builder -> builder
                .checkInDate(LocalDate.of(2025, 8, 1))
                .checkOutDate(null)
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getStayDuration()).isNull();
    }

    @Test
    @DisplayName("Should calculate same day stay duration as zero")
    void shouldCalculateSameDayStayDurationAsZero() {
        // Given - same day check-in and check-out
        Booking booking = createBookingWith(builder -> builder
                .checkInDate(LocalDate.of(2025, 8, 1))
                .checkOutDate(LocalDate.of(2025, 8, 1))
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getStayDuration()).isEqualTo(0);
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = {"CONFIRMED", "CHECKED_IN"})
    @DisplayName("Should mark booking as active for confirmed and checked-in statuses")
    void shouldMarkBookingAsActiveForActiveStatuses(BookingStatus status) {
        // Given
        Booking booking = Booking.builder()
                .id(baseBooking.getId())
                .userId(baseBooking.getUserId())
                .hotelId(baseBooking.getHotelId())
                .checkInDate(baseBooking.getCheckInDate())
                .checkOutDate(baseBooking.getCheckOutDate())
                .roomType(baseBooking.getRoomType())
                .numberOfRooms(baseBooking.getNumberOfRooms())
                .numberOfGuests(baseBooking.getNumberOfGuests())
                .totalAmount(baseBooking.getTotalAmount())
                .baseAmount(baseBooking.getBaseAmount())
                .taxesAmount(baseBooking.getTaxesAmount())
                .feesAmount(baseBooking.getFeesAmount())
                .status(status)
                .bookingReference(baseBooking.getBookingReference())
                .confirmationNumber(baseBooking.getConfirmationNumber())
                .specialRequests(baseBooking.getSpecialRequests())
                .guestName(baseBooking.getGuestName())
                .guestEmail(baseBooking.getGuestEmail())
                .guestPhone(baseBooking.getGuestPhone())
                .paymentStatus(baseBooking.getPaymentStatus())
                .paymentMethod(baseBooking.getPaymentMethod())
                .createdAt(baseBooking.getCreatedAt())
                .updatedAt(baseBooking.getUpdatedAt())
                .build();

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getIsActive()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = {"PENDING", "CANCELLED", "CHECKED_OUT", "NO_SHOW"})
    @DisplayName("Should mark booking as inactive for non-active statuses")
    void shouldMarkBookingAsInactiveForNonActiveStatuses(BookingStatus status) {
        // Given
        Booking booking = Booking.builder()
                .id(baseBooking.getId())
                .userId(baseBooking.getUserId())
                .hotelId(baseBooking.getHotelId())
                .checkInDate(baseBooking.getCheckInDate())
                .checkOutDate(baseBooking.getCheckOutDate())
                .roomType(baseBooking.getRoomType())
                .numberOfRooms(baseBooking.getNumberOfRooms())
                .numberOfGuests(baseBooking.getNumberOfGuests())
                .totalAmount(baseBooking.getTotalAmount())
                .baseAmount(baseBooking.getBaseAmount())
                .taxesAmount(baseBooking.getTaxesAmount())
                .feesAmount(baseBooking.getFeesAmount())
                .status(status)
                .bookingReference(baseBooking.getBookingReference())
                .confirmationNumber(baseBooking.getConfirmationNumber())
                .specialRequests(baseBooking.getSpecialRequests())
                .guestName(baseBooking.getGuestName())
                .guestEmail(baseBooking.getGuestEmail())
                .guestPhone(baseBooking.getGuestPhone())
                .paymentStatus(baseBooking.getPaymentStatus())
                .paymentMethod(baseBooking.getPaymentMethod())
                .createdAt(baseBooking.getCreatedAt())
                .updatedAt(baseBooking.getUpdatedAt())
                .build();

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should mark booking as upcoming when check-in date is in future")
    void shouldMarkBookingAsUpcomingWhenCheckInDateIsInFuture() {
        // Given - future check-in date
        Booking booking = createBookingWith(builder -> builder
                .checkInDate(LocalDate.now().plusDays(30))
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getIsUpcoming()).isTrue();
    }

    @Test
    @DisplayName("Should not mark booking as upcoming when check-in date is in past")
    void shouldNotMarkBookingAsUpcomingWhenCheckInDateIsInPast() {
        // Given - past check-in date
        Booking booking = createBookingWith(builder -> builder
                .checkInDate(LocalDate.now().minusDays(5))
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getIsUpcoming()).isFalse();
    }

    @Test
    @DisplayName("Should handle null check-in date for upcoming calculation")
    void shouldHandleNullCheckInDateForUpcomingCalculation() {
        // Given
        Booking booking = createBookingWith(builder -> builder
                .checkInDate(null)
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getIsUpcoming()).isFalse();
    }

    @Test
    @DisplayName("Should mark booking as past when check-out date is in past")
    void shouldMarkBookingAsPastWhenCheckOutDateIsInPast() {
        // Given - past check-out date
        Booking booking = createBookingWith(builder -> builder
                .checkOutDate(LocalDate.now().minusDays(10))
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getIsPast()).isTrue();
    }

    @Test
    @DisplayName("Should not mark booking as past when check-out date is in future")
    void shouldNotMarkBookingAsPastWhenCheckOutDateIsInFuture() {
        // Given - future check-out date
        Booking booking = createBookingWith(builder -> builder
                .checkOutDate(LocalDate.now().plusDays(10))
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getIsPast()).isFalse();
    }

    @Test
    @DisplayName("Should handle null check-out date for past calculation")
    void shouldHandleNullCheckOutDateForPastCalculation() {
        // Given
        Booking booking = createBookingWith(builder -> builder
                .checkOutDate(null)
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getIsPast()).isFalse();
    }

    @Test
    @DisplayName("Should generate group tag for bookings with more than 2 guests")
    void shouldGenerateGroupTagForBookingsWithMoreThanTwoGuests() {
        // Given
        Booking booking = createBookingWith(builder -> builder
                .numberOfGuests(5)
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getTags()).contains("group");
    }

    @Test
    @DisplayName("Should not generate group tag for bookings with 2 or fewer guests")
    void shouldNotGenerateGroupTagForBookingsWithTwoOrFewerGuests() {
        // Given
        Booking booking = createBookingWith(builder -> builder
                .numberOfGuests(2)
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getTags()).doesNotContain("group");
    }

    @Test
    @DisplayName("Should generate multi-room tag for bookings with more than 1 room")
    void shouldGenerateMultiRoomTagForBookingsWithMoreThanOneRoom() {
        // Given
        Booking booking = createBookingWith(builder -> builder
                .numberOfRooms(3)
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getTags()).contains("multi-room");
    }

    @Test
    @DisplayName("Should not generate multi-room tag for single room bookings")
    void shouldNotGenerateMultiRoomTagForSingleRoomBookings() {
        // Given
        Booking booking = createBookingWith(builder -> builder
                .numberOfRooms(1)
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getTags()).doesNotContain("multi-room");
    }

    @Test
    @DisplayName("Should generate long-stay tag for stays longer than 7 days")
    void shouldGenerateLongStayTagForStaysLongerThanSevenDays() {
        // Given - 10 days stay
        Booking booking = createBookingWith(builder -> builder
                .checkInDate(LocalDate.of(2025, 8, 1))
                .checkOutDate(LocalDate.of(2025, 8, 11))
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getTags()).contains("long-stay");
    }

    @Test
    @DisplayName("Should not generate long-stay tag for stays of 7 days or less")
    void shouldNotGenerateLongStayTagForStaysOfSevenDaysOrLess() {
        // Given - 7 days stay
        Booking booking = createBookingWith(builder -> builder
                .checkInDate(LocalDate.of(2025, 8, 1))
                .checkOutDate(LocalDate.of(2025, 8, 8))
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getTags()).doesNotContain("long-stay");
    }

    @Test
    @DisplayName("Should generate cancelled tag for cancelled bookings")
    void shouldGenerateCancelledTagForCancelledBookings() {
        // Given
        Booking booking = createBookingWith(builder -> builder
                .status(BookingStatus.CANCELLED)
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getTags()).contains("cancelled");
    }

    @Test
    @DisplayName("Should generate active-stay tag for checked-in bookings")
    void shouldGenerateActiveStayTagForCheckedInBookings() {
        // Given
        Booking booking = createBookingWith(builder -> builder
                .status(BookingStatus.CHECKED_IN)
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getTags()).contains("active-stay");
    }

    @Test
    @DisplayName("Should generate multiple tags for complex booking")
    void shouldGenerateMultipleTagsForComplexBooking() {
        // Given - large group, multi-room, long-stay booking
        Booking booking = createBookingWith(builder -> builder
                .numberOfGuests(6)
                .numberOfRooms(3)
                .checkInDate(LocalDate.of(2025, 8, 1))
                .checkOutDate(LocalDate.of(2025, 8, 15)) // 14 days
                .status(BookingStatus.CHECKED_IN)
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getTags())
                .contains("group", "multi-room", "long-stay", "active-stay")
                .hasSize(4);
    }

    @Test
    @DisplayName("Should build searchable text from all relevant fields")
    void shouldBuildSearchableTextFromAllRelevantFields() {
        // Given
        Booking booking = createBookingWith(builder -> builder
                .guestName("John Smith")
                .bookingReference("HRS202508010001")
                .confirmationNumber("CNF123456789")
                .specialRequests("Late check-in requested")
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        String searchableText = document.getSearchableText();
        assertThat(searchableText).contains("John Smith");
        assertThat(searchableText).contains("HRS202508010001");
        assertThat(searchableText).contains("CNF123456789");
        assertThat(searchableText).contains("Late check-in requested");
    }

    @Test
    @DisplayName("Should handle null fields in searchable text gracefully")
    void shouldHandleNullFieldsInSearchableTextGracefully() {
        // Given
        Booking booking = createBookingWith(builder -> builder
                .guestName(null)
                .bookingReference("HRS202508010001")
                .confirmationNumber(null)
                .specialRequests("Special request text")
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        String searchableText = document.getSearchableText();
        assertThat(searchableText).contains("HRS202508010001");
        assertThat(searchableText).contains("Special request text");
        assertThat(searchableText).doesNotContain("null");
    }

    @Test
    @DisplayName("Should handle completely empty searchable text fields")
    void shouldHandleCompletelyEmptySearchableTextFields() {
        // Given
        Booking booking = createBookingWith(builder -> builder
                .guestName(null)
                .bookingReference(null)
                .confirmationNumber(null)
                .specialRequests(null)
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getSearchableText()).isEmpty();
    }

    @Test
    @DisplayName("Should copy timestamp fields correctly")
    void shouldCopyTimestampFieldsCorrectly() {
        // Given
        LocalDateTime cancelledAt = LocalDateTime.of(2025, 7, 20, 10, 15);
        LocalDateTime checkedInAt = LocalDateTime.of(2025, 8, 1, 15, 30);
        LocalDateTime checkedOutAt = LocalDateTime.of(2025, 8, 5, 11, 0);

        Booking booking = createBookingWith(builder -> builder
                .cancelledAt(cancelledAt)
                .checkedInAt(checkedInAt)
                .checkedOutAt(checkedOutAt)
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getCancelledAt()).isEqualTo(cancelledAt);
        assertThat(document.getCheckedInAt()).isEqualTo(checkedInAt);
        assertThat(document.getCheckedOutAt()).isEqualTo(checkedOutAt);
    }

    @Test
    @DisplayName("Should handle minimal booking with only required fields")
    void shouldHandleMinimalBookingWithOnlyRequiredFields() {
        // Given - minimal booking with only essential fields
        Booking minimalBooking = Booking.builder()
                .id(1L)
                .userId(101L)
                .hotelId(201L)
                .checkInDate(LocalDate.of(2025, 8, 1))
                .checkOutDate(LocalDate.of(2025, 8, 3))
                .numberOfRooms(1)
                .numberOfGuests(1)
                .status(BookingStatus.CONFIRMED)
                .bookingReference("HRS202508010001")
                .build();

        // When
        BookingSearchDocument document = mapper.toSearchDocument(minimalBooking);

        // Then
        assertThat(document).isNotNull();
        assertThat(document.getId()).isEqualTo(1L);
        assertThat(document.getUserId()).isEqualTo(101L);
        assertThat(document.getHotelId()).isEqualTo(201L);
        assertThat(document.getStayDuration()).isEqualTo(2);
        assertThat(document.getIsActive()).isTrue();
        assertThat(document.getTags()).isEmpty(); // No special conditions met
        assertThat(document.getSearchableText()).contains("HRS202508010001");
    }

    @Test
    @DisplayName("Should handle edge case of zero guests")
    void shouldHandleEdgeCaseOfZeroGuests() {
        // Given
        Booking booking = createBookingWith(builder -> builder
                .numberOfGuests(0)
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getNumberOfGuests()).isEqualTo(0);
        assertThat(document.getTags()).doesNotContain("group");
    }

    @Test
    @DisplayName("Should handle edge case of zero rooms")
    void shouldHandleEdgeCaseOfZeroRooms() {
        // Given
        Booking booking = createBookingWith(builder -> builder
                .numberOfRooms(0)
        );

        // When
        BookingSearchDocument document = mapper.toSearchDocument(booking);

        // Then
        assertThat(document.getNumberOfRooms()).isEqualTo(0);
        assertThat(document.getTags()).doesNotContain("multi-room");
    }
}
