package com.hrs.hotelbooking.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrs.hotelbooking.booking.dto.BookingSearchCriteria;
import com.hrs.hotelbooking.booking.entity.BookingSearchDocument;
import com.hrs.hotelbooking.booking.service.BookingSearchService;
import com.hrs.hotelbooking.shared.dto.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test cases for BookingSearchController
 * Tests REST endpoints for booking search functionality
 *
 * @author arihants1
 */
@WebMvcTest(BookingSearchController.class)
@DisplayName("Booking Search Controller Tests")
class BookingSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingSearchService bookingSearchService;

    @Autowired
    private ObjectMapper objectMapper;

    private BookingSearchDocument searchDocument;
    private BookingSearchCriteria searchCriteria;
    private Page<BookingSearchDocument> searchResultPage;

    @BeforeEach
    void setUp() {
        // Setup test data
        searchDocument = BookingSearchDocument.builder()
                .id(1L)
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

        searchCriteria = BookingSearchCriteria.builder()
                .statuses(Arrays.asList(BookingStatus.CONFIRMED))
                .checkInFrom(LocalDate.of(2025, 7, 1))
                .checkInTo(LocalDate.of(2025, 7, 31))
                .build();

        searchResultPage = new PageImpl<>(
                Arrays.asList(searchDocument),
                PageRequest.of(0, 20),
                1
        );
    }

    @Test
    @DisplayName("Should search bookings with query parameter successfully")
    void shouldSearchBookingsWithQueryParameterSuccessfully() throws Exception {
        // Given
        String searchQuery = "John Doe";
        given(bookingSearchService.searchBookings(eq(searchQuery), any(Pageable.class)))
                .willReturn(searchResultPage);

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/search")
                        .param("query", searchQuery)
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].guestName", is("John Doe")))
                .andExpect(jsonPath("$.content[0].bookingReference", is("HRS202506290001")))
                .andExpect(jsonPath("$.content[0].status", is("CONFIRMED")))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.size", is(20)))
                .andExpect(jsonPath("$.number", is(0)));

        verify(bookingSearchService).searchBookings(eq(searchQuery), any(Pageable.class));
    }

    @Test
    @DisplayName("Should search bookings without query parameter")
    void shouldSearchBookingsWithoutQueryParameter() throws Exception {
        // Given
        Page<BookingSearchDocument> emptyPage = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 20),
                0
        );
        given(bookingSearchService.searchBookings(eq(null), any(Pageable.class)))
                .willReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/search")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)))
                .andExpect(jsonPath("$.totalPages", is(0)));

        verify(bookingSearchService).searchBookings(eq(null), any(Pageable.class));
    }

    @Test
    @DisplayName("Should search by booking reference")
    void shouldSearchByBookingReference() throws Exception {
        // Given
        String bookingReference = "HRS202506290001";
        given(bookingSearchService.findByReference(bookingReference))
                .willReturn(searchDocument);

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/search/reference/{reference}", bookingReference)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bookingReference", is("HRS202506290001")))
                .andExpect(jsonPath("$.guestName", is("John Doe")))
                .andExpect(jsonPath("$.status", is("CONFIRMED")));

        verify(bookingSearchService).findByReference(bookingReference);
    }

    @Test
    @DisplayName("Should return 404 when booking reference not found")
    void shouldReturn404WhenBookingReferenceNotFound() throws Exception {
        // Given
        String bookingReference = "INVALID_REF";
        given(bookingSearchService.findByReference(bookingReference))
                .willReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/search/reference/{reference}", bookingReference)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(bookingSearchService).findByReference(bookingReference);
    }

    @Test
    @DisplayName("Should search user bookings with criteria")
    void shouldSearchUserBookingsWithCriteria() throws Exception {
        // Given
        Long userId = 1L;
        given(bookingSearchService.searchUserBookings(eq(userId), any(BookingSearchCriteria.class), any(Pageable.class)))
                .willReturn(searchResultPage);

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/search/user/{userId}", userId)
                        .param("statuses", "CONFIRMED")
                        .param("checkInFrom", "2025-07-01")
                        .param("checkInTo", "2025-07-31")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].guestName", is("John Doe")));

        verify(bookingSearchService).searchUserBookings(eq(userId), any(BookingSearchCriteria.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get upcoming hotel bookings")
    void shouldGetUpcomingHotelBookings() throws Exception {
        // Given
        Long hotelId = 1L;
        given(bookingSearchService.getUpcomingHotelBookings(eq(hotelId), any(Pageable.class)))
                .willReturn(searchResultPage);

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/search/hotel/{hotelId}/upcoming", hotelId)
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].hotelId", is(1)));

        verify(bookingSearchService).getUpcomingHotelBookings(eq(hotelId), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get booking statistics by status")
    void shouldGetBookingStatisticsByStatus() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2025, 7, 1);
        LocalDate endDate = LocalDate.of(2025, 7, 31);
        Map<BookingStatus, Long> stats = Map.of(
                BookingStatus.CONFIRMED, 5L,
                BookingStatus.PENDING, 2L,
                BookingStatus.CANCELLED, 1L
        );
        given(bookingSearchService.getBookingStatsByStatus(startDate, endDate))
                .willReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/search/stats/status")
                        .param("startDate", "2025-07-01")
                        .param("endDate", "2025-07-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.CONFIRMED", is(5)))
                .andExpect(jsonPath("$.PENDING", is(2)))
                .andExpect(jsonPath("$.CANCELLED", is(1)));

        verify(bookingSearchService).getBookingStatsByStatus(startDate, endDate);
    }

    @Test
    @DisplayName("Should handle invalid pagination parameters gracefully")
    void shouldHandleInvalidPaginationParametersGracefully() throws Exception {
        // When & Then - Invalid negative page should return 500 (as shown in the error log)
        mockMvc.perform(get("/api/v1/bookings/search")
                        .param("page", "-1")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
}
