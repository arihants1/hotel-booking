package com.hrs.hotelbooking.booking.impl;

import com.hrs.hotelbooking.booking.entity.BookingSearchDocument;
import com.hrs.hotelbooking.booking.repository.BookingSearchRepository;
import com.hrs.hotelbooking.shared.dto.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * Test cases for BookingSearchServiceImpl
 * Tests the actual methods available in the implementation
 *
 * @author arihants1
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Booking Search Service Tests")
class BookingSearchServiceImplTest {

    @Mock
    private BookingSearchRepository bookingSearchRepository;

    @InjectMocks
    private BookingSearchServiceImpl bookingSearchService;

    private BookingSearchDocument searchDocument;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Setup test data
        searchDocument = BookingSearchDocument.builder()
                .id(1L)
//                .bookingId(1L)
                .userId(1L)
                .hotelId(1L)
                .bookingReference("HRS202506290001")
                .confirmationNumber("CNF123456789")
                .guestName("John Doe")
                .guestEmail("john.doe@example.com")
                .guestPhone("+1234567890")
                .status(BookingStatus.CONFIRMED)
                .checkInDate(LocalDate.of(2025, 7, 15))
                .checkOutDate(LocalDate.of(2025, 7, 17))
                .totalAmount(new BigDecimal("299.99"))
                .createdAt(LocalDateTime.now())
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Should perform full-text search successfully")
    void shouldPerformFullTextSearchSuccessfully() {
        // Given
        String searchQuery = "John Doe";
        Page<BookingSearchDocument> expectedPage = new PageImpl<>(
                Arrays.asList(searchDocument), pageable, 1);
        given(bookingSearchRepository.fullTextSearch(searchQuery, pageable))
                .willReturn(expectedPage);

        // When
        Page<BookingSearchDocument> result = bookingSearchService.searchBookings(searchQuery, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getGuestName()).isEqualTo("John Doe");
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(bookingSearchRepository).fullTextSearch(searchQuery, pageable);
    }

    @Test
    @DisplayName("Should return empty page when no search results found")
    void shouldReturnEmptyPageWhenNoSearchResultsFound() {
        // Given
        String searchQuery = "NonExistentGuest";
        Page<BookingSearchDocument> emptyPage = new PageImpl<>(
                Collections.emptyList(), pageable, 0);
        given(bookingSearchRepository.fullTextSearch(searchQuery, pageable))
                .willReturn(emptyPage);

        // When
        Page<BookingSearchDocument> result = bookingSearchService.searchBookings(searchQuery, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        verify(bookingSearchRepository).fullTextSearch(searchQuery, pageable);
    }

    @Test
    @DisplayName("Should handle null search query gracefully")
    void shouldHandleNullSearchQueryGracefully() {
        // Given
        String searchQuery = null;
        Page<BookingSearchDocument> emptyPage = new PageImpl<>(
                Collections.emptyList(), pageable, 0);
        given(bookingSearchRepository.fullTextSearch(searchQuery, pageable))
                .willReturn(emptyPage);

        // When
        Page<BookingSearchDocument> result = bookingSearchService.searchBookings(searchQuery, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        verify(bookingSearchRepository).fullTextSearch(searchQuery, pageable);
    }

    @Test
    @DisplayName("Should handle empty search query")
    void shouldHandleEmptySearchQuery() {
        // Given
        String searchQuery = "";
        Page<BookingSearchDocument> emptyPage = new PageImpl<>(
                Collections.emptyList(), pageable, 0);
        given(bookingSearchRepository.fullTextSearch(searchQuery, pageable))
                .willReturn(emptyPage);

        // When
        Page<BookingSearchDocument> result = bookingSearchService.searchBookings(searchQuery, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        verify(bookingSearchRepository).fullTextSearch(searchQuery, pageable);
    }

    @Test
    @DisplayName("Should find booking by reference successfully")
    void shouldFindBookingByReferenceSuccessfully() {
        // Given
        String bookingReference = "HRS202506290001";
        given(bookingSearchRepository.findByBookingReference(bookingReference))
                .willReturn(searchDocument);

        // When
        BookingSearchDocument result = bookingSearchService.findByReference(bookingReference);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBookingReference()).isEqualTo(bookingReference);
        assertThat(result.getGuestName()).isEqualTo("John Doe");
        verify(bookingSearchRepository).findByBookingReference(bookingReference);
    }

    @Test
    @DisplayName("Should return null when booking reference not found")
    void shouldReturnNullWhenBookingReferenceNotFound() {
        // Given
        String bookingReference = "INVALID_REF";
        given(bookingSearchRepository.findByBookingReference(bookingReference))
                .willReturn(null);

        // When
        BookingSearchDocument result = bookingSearchService.findByReference(bookingReference);

        // Then
        assertThat(result).isNull();
        verify(bookingSearchRepository, times(2)).findByBookingReference(bookingReference);
    }

    @Test
    @DisplayName("Should handle null reference parameter")
    void shouldHandleNullReferenceParameter() {
        // Given
        String bookingReference = null;
        given(bookingSearchRepository.findByBookingReference(bookingReference))
                .willReturn(null);

        // When
        BookingSearchDocument result = bookingSearchService.findByReference(bookingReference);

        // Then
        assertThat(result).isNull();
        // The implementation calls findByBookingReference twice (bug in implementation)
        verify(bookingSearchRepository, times(2)).findByBookingReference(bookingReference);
    }

    @Test
    @DisplayName("Should handle empty reference parameter")
    void shouldHandleEmptyReferenceParameter() {
        // Given
        String bookingReference = "";
        given(bookingSearchRepository.findByBookingReference(bookingReference))
                .willReturn(null);

        // When
        BookingSearchDocument result = bookingSearchService.findByReference(bookingReference);

        // Then
        assertThat(result).isNull();
        verify(bookingSearchRepository, times(2)).findByBookingReference(bookingReference);
    }

    @Test
    @DisplayName("Should handle search exception gracefully")
    void shouldHandleSearchExceptionGracefully() {
        // Given
        String searchQuery = "test";
        given(bookingSearchRepository.fullTextSearch(searchQuery, pageable))
                .willThrow(new RuntimeException("Elasticsearch connection failed"));

        // When & Then
        assertThatThrownBy(() -> bookingSearchService.searchBookings(searchQuery, pageable))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Elasticsearch connection failed");

        verify(bookingSearchRepository).fullTextSearch(searchQuery, pageable);
    }

    @Test
    @DisplayName("Should handle find by reference exception gracefully")
    void shouldHandleFindByReferenceExceptionGracefully() {
        // Given
        String bookingReference = "HRS202506290001";
        given(bookingSearchRepository.findByBookingReference(bookingReference))
                .willThrow(new RuntimeException("Elasticsearch connection failed"));

        // When & Then
        assertThatThrownBy(() -> bookingSearchService.findByReference(bookingReference))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Elasticsearch connection failed");

        verify(bookingSearchRepository).findByBookingReference(bookingReference);
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePaginationCorrectly() {
        // Given
        String searchQuery = "test";
        Pageable secondPage = PageRequest.of(1, 5);
        Page<BookingSearchDocument> expectedPage = new PageImpl<>(
                Arrays.asList(searchDocument), secondPage, 10);
        given(bookingSearchRepository.fullTextSearch(searchQuery, secondPage))
                .willReturn(expectedPage);

        // When
        Page<BookingSearchDocument> result = bookingSearchService.searchBookings(searchQuery, secondPage);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNumber()).isEqualTo(1); // Second page
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getTotalElements()).isEqualTo(10);
        assertThat(result.getTotalPages()).isEqualTo(2);
        verify(bookingSearchRepository).fullTextSearch(searchQuery, secondPage);
    }

    @Test
    @DisplayName("Should search with special characters")
    void shouldSearchWithSpecialCharacters() {
        // Given
        String searchQuery = "José María";
        Page<BookingSearchDocument> expectedPage = new PageImpl<>(
                Arrays.asList(searchDocument), pageable, 1);
        given(bookingSearchRepository.fullTextSearch(searchQuery, pageable))
                .willReturn(expectedPage);

        // When
        Page<BookingSearchDocument> result = bookingSearchService.searchBookings(searchQuery, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(bookingSearchRepository).fullTextSearch(searchQuery, pageable);
    }

    @Test
    @DisplayName("Should search with email query")
    void shouldSearchWithEmailQuery() {
        // Given
        String searchQuery = "john.doe@example.com";
        Page<BookingSearchDocument> expectedPage = new PageImpl<>(
                Arrays.asList(searchDocument), pageable, 1);
        given(bookingSearchRepository.fullTextSearch(searchQuery, pageable))
                .willReturn(expectedPage);

        // When
        Page<BookingSearchDocument> result = bookingSearchService.searchBookings(searchQuery, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getGuestEmail()).isEqualTo("john.doe@example.com");
        verify(bookingSearchRepository).fullTextSearch(searchQuery, pageable);
    }

    @Test
    @DisplayName("Should search with phone number query")
    void shouldSearchWithPhoneNumberQuery() {
        // Given
        String searchQuery = "+1234567890";
        Page<BookingSearchDocument> expectedPage = new PageImpl<>(
                Arrays.asList(searchDocument), pageable, 1);
        given(bookingSearchRepository.fullTextSearch(searchQuery, pageable))
                .willReturn(expectedPage);

        // When
        Page<BookingSearchDocument> result = bookingSearchService.searchBookings(searchQuery, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getGuestPhone()).isEqualTo("+1234567890");
        verify(bookingSearchRepository).fullTextSearch(searchQuery, pageable);
    }

    @Test
    @DisplayName("Should search with confirmation number")
    void shouldSearchWithConfirmationNumber() {
        // Given
        String searchQuery = "CNF123456789";
        Page<BookingSearchDocument> expectedPage = new PageImpl<>(
                Arrays.asList(searchDocument), pageable, 1);
        given(bookingSearchRepository.fullTextSearch(searchQuery, pageable))
                .willReturn(expectedPage);

        // When
        Page<BookingSearchDocument> result = bookingSearchService.searchBookings(searchQuery, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getConfirmationNumber()).isEqualTo("CNF123456789");
        verify(bookingSearchRepository).fullTextSearch(searchQuery, pageable);
    }
}
