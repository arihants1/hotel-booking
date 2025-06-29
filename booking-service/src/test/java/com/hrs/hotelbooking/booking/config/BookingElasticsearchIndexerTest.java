package com.hrs.hotelbooking.booking.config;

import com.hrs.hotelbooking.booking.entity.Booking;
import com.hrs.hotelbooking.booking.entity.BookingSearchDocument;
import com.hrs.hotelbooking.booking.repository.BookingRepository;
import com.hrs.hotelbooking.booking.repository.BookingSearchRepository;
import com.hrs.hotelbooking.shared.dto.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Test cases for BookingElasticsearchIndexer
 * Tests the synchronization of booking data from PostgreSQL to Elasticsearch
 *
 * @author arihants1
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Booking Elasticsearch Indexer Tests")
class BookingElasticsearchIndexerTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingSearchRepository bookingSearchRepository;

    @InjectMocks
    private BookingElasticsearchIndexer indexer;

    @Captor
    private ArgumentCaptor<List<BookingSearchDocument>> documentsCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    private Booking sampleBooking1;
    private Booking sampleBooking2;
    private Booking sampleBooking3;

    @BeforeEach
    void setUp() {
        // Set batch size for testing
        ReflectionTestUtils.setField(indexer, "batchSize", 2);

        // Create sample bookings with different characteristics
        sampleBooking1 = Booking.builder()
                .id(1L)
                .userId(101L)
                .hotelId(201L)
                .checkInDate(LocalDate.of(2025, 8, 1))
                .checkOutDate(LocalDate.of(2025, 8, 5))
                .roomType("Standard")
                .numberOfRooms(1)
                .numberOfGuests(2)
                .totalAmount(new BigDecimal("299.99"))
                .baseAmount(new BigDecimal("250.00"))
                .taxesAmount(new BigDecimal("37.50"))
                .feesAmount(new BigDecimal("12.49"))
                .status(BookingStatus.CONFIRMED)
                .bookingReference("HRS202508010001")
                .confirmationNumber("CNF123456789")
                .guestName("John Doe")
                .guestEmail("john.doe@example.com")
                .guestPhone("+1234567890")
                .paymentStatus("PAID")
                .paymentMethod("CREDIT_CARD")
                .createdAt(LocalDateTime.now().minusDays(5))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build();

        sampleBooking2 = Booking.builder()
                .id(2L)
                .userId(102L)
                .hotelId(202L)
                .checkInDate(LocalDate.of(2025, 8, 15))
                .checkOutDate(LocalDate.of(2025, 8, 25)) // 10 days - long stay
                .roomType("Suite")
                .numberOfRooms(2) // multi-room
                .numberOfGuests(4) // group
                .totalAmount(new BigDecimal("1299.99"))
                .baseAmount(new BigDecimal("1100.00"))
                .taxesAmount(new BigDecimal("165.00"))
                .feesAmount(new BigDecimal("34.99"))
                .status(BookingStatus.CHECKED_IN) // active stay
                .bookingReference("HRS202508150001")
                .confirmationNumber("CNF987654321")
                .specialRequests("High floor, ocean view")
                .guestName("Jane Smith")
                .guestEmail("jane.smith@example.com")
                .guestPhone("+9876543210")
                .paymentStatus("PAID")
                .paymentMethod("DEBIT_CARD")
                .checkedInAt(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now().minusDays(10))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        sampleBooking3 = Booking.builder()
                .id(3L)
                .userId(103L)
                .hotelId(203L)
                .checkInDate(LocalDate.of(2025, 7, 1))
                .checkOutDate(LocalDate.of(2025, 7, 3))
                .roomType("Economy")
                .numberOfRooms(1)
                .numberOfGuests(1)
                .totalAmount(new BigDecimal("150.00"))
                .status(BookingStatus.CANCELLED) // cancelled
                .bookingReference("HRS202507010001")
                .guestName("Bob Wilson")
                .guestEmail("bob.wilson@example.com")
                .cancelledAt(LocalDateTime.now().minusDays(20))
                .createdAt(LocalDateTime.now().minusDays(25))
                .updatedAt(LocalDateTime.now().minusDays(20))
                .build();
    }

    @Test
    @DisplayName("Should process bookings successfully when database has data")
    void shouldProcessBookingsSuccessfullyWhenDatabaseHasData() throws Exception {
        // Given
        List<Booking> firstBatch = Arrays.asList(sampleBooking1, sampleBooking2);
        List<Booking> secondBatch = Collections.singletonList(sampleBooking3);

        Page<Booking> firstPage = new PageImpl<>(firstBatch, PageRequest.of(0, 2), 3);
        Page<Booking> secondPage = new PageImpl<>(secondBatch, PageRequest.of(1, 2), 3);

        given(bookingRepository.count()).willReturn(3L);
        given(bookingRepository.findAll(PageRequest.of(0, 2))).willReturn(firstPage);
        given(bookingRepository.findAll(PageRequest.of(1, 2))).willReturn(secondPage);

        // Mock existence checks - booking1 exists (update), booking2 and booking3 are new
        given(bookingSearchRepository.existsById(1L)).willReturn(true);
        given(bookingSearchRepository.existsById(2L)).willReturn(false);
        given(bookingSearchRepository.existsById(3L)).willReturn(false);

        // When
        indexer.run();

        // Then
        verify(bookingRepository).count();
        verify(bookingRepository, times(2)).findAll(any(Pageable.class));
        verify(bookingSearchRepository, times(3)).existsById(anyLong());
        verify(bookingSearchRepository, times(2)).saveAll(documentsCaptor.capture());

        List<List<BookingSearchDocument>> allBatches = documentsCaptor.getAllValues();
        assertThat(allBatches).hasSize(2);
        assertThat(allBatches.get(0)).hasSize(2); // First batch
        assertThat(allBatches.get(1)).hasSize(1); // Second batch

        // Verify first batch documents
        List<BookingSearchDocument> firstBatchDocs = allBatches.get(0);
        BookingSearchDocument doc1 = firstBatchDocs.get(0);
        BookingSearchDocument doc2 = firstBatchDocs.get(1);

        assertThat(doc1.getId()).isEqualTo(1L);
        assertThat(doc1.getStayDuration()).isEqualTo(4);
        assertThat(doc1.getIsActive()).isTrue();
        assertThat(doc1.getTags()).doesNotContain("group", "multi-room", "long-stay");

        assertThat(doc2.getId()).isEqualTo(2L);
        assertThat(doc2.getStayDuration()).isEqualTo(10);
        assertThat(doc2.getIsActive()).isTrue();
        assertThat(doc2.getTags()).contains("group", "multi-room", "long-stay", "active-stay");

        // Verify second batch documents
        List<BookingSearchDocument> secondBatchDocs = allBatches.get(1);
        BookingSearchDocument doc3 = secondBatchDocs.get(0);

        assertThat(doc3.getId()).isEqualTo(3L);
        assertThat(doc3.getStayDuration()).isEqualTo(2);
        assertThat(doc3.getIsActive()).isFalse();
        assertThat(doc3.getTags()).contains("cancelled");
    }

    @Test
    @DisplayName("Should handle empty database gracefully")
    void shouldHandleEmptyDatabaseGracefully() throws Exception {
        // Given
        given(bookingRepository.count()).willReturn(0L);

        // When
        indexer.run();

        // Then
        verify(bookingRepository).count();
        verify(bookingRepository, never()).findAll(any(Pageable.class));
        verify(bookingSearchRepository, never()).existsById(anyLong());
        verify(bookingSearchRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should handle single page of results")
    void shouldHandleSinglePageOfResults() throws Exception {
        // Given
        List<Booking> bookings = Collections.singletonList(sampleBooking1);
        Page<Booking> singlePage = new PageImpl<>(bookings, PageRequest.of(0, 2), 1);

        given(bookingRepository.count()).willReturn(1L);
        given(bookingRepository.findAll(PageRequest.of(0, 2))).willReturn(singlePage);
        given(bookingSearchRepository.existsById(1L)).willReturn(false);

        // When
        indexer.run();

        // Then
        verify(bookingRepository).count();
        verify(bookingRepository, times(1)).findAll(any(Pageable.class));
        verify(bookingSearchRepository, times(1)).existsById(1L);
        verify(bookingSearchRepository, times(1)).saveAll(documentsCaptor.capture());

        List<BookingSearchDocument> savedDocs = documentsCaptor.getValue();
        assertThat(savedDocs).hasSize(1);
        assertThat(savedDocs.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should use correct batch size from configuration")
    void shouldUseCorrectBatchSizeFromConfiguration() throws Exception {
        // Given
        ReflectionTestUtils.setField(indexer, "batchSize", 5);

        given(bookingRepository.count()).willReturn(1L);
        given(bookingRepository.findAll(any(Pageable.class)))
                .willReturn(new PageImpl<>(Collections.singletonList(sampleBooking1), PageRequest.of(0, 5), 1));
        given(bookingSearchRepository.existsById(anyLong())).willReturn(false);

        // When
        indexer.run();

        // Then
        verify(bookingRepository).findAll(pageableCaptor.capture());
        Pageable usedPageable = pageableCaptor.getValue();
        assertThat(usedPageable.getPageSize()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should map booking to search document correctly")
    void shouldMapBookingToSearchDocumentCorrectly() throws Exception {
        // Given
        given(bookingRepository.count()).willReturn(1L);
        given(bookingRepository.findAll(any(Pageable.class)))
                .willReturn(new PageImpl<>(Collections.singletonList(sampleBooking2), PageRequest.of(0, 2), 1));
        given(bookingSearchRepository.existsById(anyLong())).willReturn(false);

        // When
        indexer.run();

        // Then
        verify(bookingSearchRepository).saveAll(documentsCaptor.capture());
        BookingSearchDocument document = documentsCaptor.getValue().get(0);

        // Verify basic mapping
        assertThat(document.getId()).isEqualTo(sampleBooking2.getId());
        assertThat(document.getUserId()).isEqualTo(sampleBooking2.getUserId());
        assertThat(document.getHotelId()).isEqualTo(sampleBooking2.getHotelId());
        assertThat(document.getGuestName()).isEqualTo(sampleBooking2.getGuestName());
        assertThat(document.getBookingReference()).isEqualTo(sampleBooking2.getBookingReference());

        // Verify derived fields
        assertThat(document.getStayDuration()).isEqualTo(10);
        assertThat(document.getIsActive()).isTrue();
        assertThat(document.getIsUpcoming()).isTrue(); // Future check-in date
        assertThat(document.getIsPast()).isFalse();

        // Verify tags
        assertThat(document.getTags()).contains("group", "multi-room", "long-stay", "active-stay");

        // Verify searchable text
        String searchableText = document.getSearchableText();
        assertThat(searchableText).contains("Jane Smith");
        assertThat(searchableText).contains("HRS202508150001");
        assertThat(searchableText).contains("CNF987654321");
        assertThat(searchableText).contains("High floor, ocean view");
    }

    @Test
    @DisplayName("Should handle bookings with null dates")
    void shouldHandleBookingsWithNullDates() throws Exception {
        // Given
        Booking bookingWithNullDates = Booking.builder()
                .id(4L)
                .userId(104L)
                .hotelId(204L)
                .checkInDate(null)
                .checkOutDate(null)
                .roomType("Standard")
                .numberOfRooms(1)
                .numberOfGuests(1)
                .status(BookingStatus.PENDING)
                .bookingReference("HRS202508010004")
                .guestName("Test User")
                .build();

        given(bookingRepository.count()).willReturn(1L);
        given(bookingRepository.findAll(any(Pageable.class)))
                .willReturn(new PageImpl<>(Collections.singletonList(bookingWithNullDates), PageRequest.of(0, 2), 1));
        given(bookingSearchRepository.existsById(anyLong())).willReturn(false);

        // When
        indexer.run();

        // Then
        verify(bookingSearchRepository).saveAll(documentsCaptor.capture());
        BookingSearchDocument document = documentsCaptor.getValue().get(0);

        assertThat(document.getStayDuration()).isNull();
        assertThat(document.getIsUpcoming()).isFalse();
        assertThat(document.getIsPast()).isFalse();
    }

    @Test
    @DisplayName("Should handle bookings with minimal data")
    void shouldHandleBookingsWithMinimalData() throws Exception {
        // Given
        Booking minimalBooking = Booking.builder()
                .id(5L)
                .userId(105L)
                .hotelId(205L)
                .checkInDate(LocalDate.of(2025, 9, 1))
                .checkOutDate(LocalDate.of(2025, 9, 2))
                .numberOfRooms(1)
                .numberOfGuests(1)
                .status(BookingStatus.CONFIRMED)
                .bookingReference("HRS202509010001")
                .build();

        given(bookingRepository.count()).willReturn(1L);
        given(bookingRepository.findAll(any(Pageable.class)))
                .willReturn(new PageImpl<>(Collections.singletonList(minimalBooking), PageRequest.of(0, 2), 1));
        given(bookingSearchRepository.existsById(anyLong())).willReturn(false);

        // When
        indexer.run();

        // Then
        verify(bookingSearchRepository).saveAll(documentsCaptor.capture());
        BookingSearchDocument document = documentsCaptor.getValue().get(0);

        assertThat(document.getId()).isEqualTo(5L);
        assertThat(document.getStayDuration()).isEqualTo(1);
        assertThat(document.getIsActive()).isTrue();
        assertThat(document.getTags()).isEmpty(); // No special conditions met
        assertThat(document.getSearchableText()).contains("HRS202509010001");
    }

    @Test
    @DisplayName("Should calculate derived fields correctly for past booking")
    void shouldCalculateDerivedFieldsCorrectlyForPastBooking() throws Exception {
        // Given - past booking
        Booking pastBooking = Booking.builder()
                .id(6L)
                .userId(106L)
                .hotelId(206L)
                .checkInDate(LocalDate.now().minusDays(10))
                .checkOutDate(LocalDate.now().minusDays(7))
                .numberOfRooms(1)
                .numberOfGuests(1)
                .status(BookingStatus.CHECKED_OUT)
                .bookingReference("HRS202506190001")
                .guestName("Past Guest")
                .build();

        given(bookingRepository.count()).willReturn(1L);
        given(bookingRepository.findAll(any(Pageable.class)))
                .willReturn(new PageImpl<>(Collections.singletonList(pastBooking), PageRequest.of(0, 2), 1));
        given(bookingSearchRepository.existsById(anyLong())).willReturn(false);

        // When
        indexer.run();

        // Then
        verify(bookingSearchRepository).saveAll(documentsCaptor.capture());
        BookingSearchDocument document = documentsCaptor.getValue().get(0);

        assertThat(document.getStayDuration()).isEqualTo(3);
        assertThat(document.getIsActive()).isFalse(); // CHECKED_OUT is not active
        assertThat(document.getIsUpcoming()).isFalse(); // Past check-in date
        assertThat(document.getIsPast()).isTrue(); // Past check-out date
    }

    @Test
    @DisplayName("Should handle future booking correctly")
    void shouldHandleFutureBookingCorrectly() throws Exception {
        // Given - future booking
        Booking futureBooking = Booking.builder()
                .id(7L)
                .userId(107L)
                .hotelId(207L)
                .checkInDate(LocalDate.now().plusDays(30))
                .checkOutDate(LocalDate.now().plusDays(33))
                .numberOfRooms(1)
                .numberOfGuests(1)
                .status(BookingStatus.CONFIRMED)
                .bookingReference("HRS202507290001")
                .guestName("Future Guest")
                .build();

        given(bookingRepository.count()).willReturn(1L);
        given(bookingRepository.findAll(any(Pageable.class)))
                .willReturn(new PageImpl<>(Collections.singletonList(futureBooking), PageRequest.of(0, 2), 1));
        given(bookingSearchRepository.existsById(anyLong())).willReturn(false);

        // When
        indexer.run();

        // Then
        verify(bookingSearchRepository).saveAll(documentsCaptor.capture());
        BookingSearchDocument document = documentsCaptor.getValue().get(0);

        assertThat(document.getStayDuration()).isEqualTo(3);
        assertThat(document.getIsActive()).isTrue(); // CONFIRMED is active
        assertThat(document.getIsUpcoming()).isTrue(); // Future check-in date
        assertThat(document.getIsPast()).isFalse(); // Future check-out date
    }

    @Test
    @DisplayName("Should skip saving when batch is empty")
    void shouldSkipSavingWhenBatchIsEmpty() throws Exception {
        // Given - empty page
        Page<Booking> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 2), 0);

        given(bookingRepository.count()).willReturn(0L);

        // When
        indexer.run();

        // Then
        verify(bookingSearchRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should process multiple pages correctly")
    void shouldProcessMultiplePagesCorrectly() throws Exception {
        // Given
        List<Booking> page1 = Arrays.asList(sampleBooking1, sampleBooking2);
        List<Booking> page2 = Collections.singletonList(sampleBooking3);

        Page<Booking> firstPage = new PageImpl<>(page1, PageRequest.of(0, 2), 3);
        Page<Booking> secondPage = new PageImpl<>(page2, PageRequest.of(1, 2), 3);

        given(bookingRepository.count()).willReturn(3L);
        given(bookingRepository.findAll(PageRequest.of(0, 2))).willReturn(firstPage);
        given(bookingRepository.findAll(PageRequest.of(1, 2))).willReturn(secondPage);
        given(bookingSearchRepository.existsById(anyLong())).willReturn(false);

        // When
        indexer.run();

        // Then
        verify(bookingRepository, times(2)).findAll(pageableCaptor.capture());
        List<Pageable> pageables = pageableCaptor.getAllValues();

        assertThat(pageables.get(0).getPageNumber()).isEqualTo(0);
        assertThat(pageables.get(1).getPageNumber()).isEqualTo(1);

        verify(bookingSearchRepository, times(2)).saveAll(any());
    }
}
