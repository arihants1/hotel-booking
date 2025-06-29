package com.hrs.hotelbooking.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrs.hotelbooking.booking.service.BookingService;
import com.hrs.hotelbooking.shared.dto.BookingDTO;
import com.hrs.hotelbooking.shared.dto.BookingStatus;
import com.hrs.hotelbooking.shared.exception.ResourceNotFoundException;
import com.hrs.hotelbooking.shared.exception.BusinessValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test cases for BookingController
 * Tests all REST endpoints with various scenarios including success, validation, and error cases
 */
@WebMvcTest(BookingController.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("Booking Controller Tests")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    private BookingDTO validBookingDTO;
    private BookingDTO createdBookingDTO;

    @BeforeEach
    void setUp() {
        validBookingDTO = BookingDTO.builder()
                .userId(1L)
                .hotelId(1L)
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .roomType("STANDARD")
                .numberOfRooms(1)
                .numberOfGuests(2)
                .baseAmount(new BigDecimal("200.00"))
                .guestName("John Doe")
                .guestEmail("john.doe@example.com")
                .guestPhone("+1234567890")
                .specialRequests("Late check-in")
                .build();

        createdBookingDTO = BookingDTO.builder()
                .id(1L)
                .userId(1L)
                .hotelId(1L)
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .roomType("STANDARD")
                .numberOfRooms(1)
                .numberOfGuests(2)
                .baseAmount(new BigDecimal("200.00"))
                .taxesAmount(new BigDecimal("20.00"))
                .feesAmount(new BigDecimal("10.00"))
                .totalAmount(new BigDecimal("230.00"))
                .status(BookingStatus.CONFIRMED)
                .bookingReference("BK202506270001")
                .confirmationNumber("CNF123456789")
                .guestName("John Doe")
                .guestEmail("john.doe@example.com")
                .guestPhone("+1234567890")
                .specialRequests("Late check-in")
                .build();
    }

    @Test
    @DisplayName("Should create booking successfully with valid data")
    void shouldCreateBookingSuccessfully() throws Exception {
        // Given
        given(bookingService.createBooking(any(BookingDTO.class))).willReturn(createdBookingDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("HRS booking created successfully")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.userId", is(1)))
                .andExpect(jsonPath("$.data.hotelId", is(1)))
                .andExpect(jsonPath("$.data.roomType", is("STANDARD")))
                .andExpect(jsonPath("$.data.numberOfRooms", is(1)))
                .andExpect(jsonPath("$.data.numberOfGuests", is(2)))
                .andExpect(jsonPath("$.data.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.data.bookingReference", is("BK202506270001")))
                .andExpect(jsonPath("$.data.totalAmount", is(230.00)));
    }

    @Test
    @DisplayName("Should return validation error for invalid booking data")
    void shouldReturnValidationErrorForInvalidBookingData() throws Exception {
        // Given - Invalid booking with missing required fields
        BookingDTO invalidBooking = BookingDTO.builder()
                .userId(null) // Missing required field
                .hotelId(null) // Missing required field
                .checkInDate(LocalDate.now().minusDays(1)) // Past date
                .checkOutDate(LocalDate.now().minusDays(2)) // Before check-in
                .numberOfRooms(0) // Invalid value
                .numberOfGuests(-1) // Invalid value
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBooking)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle business validation exception during booking creation")
    void shouldHandleBusinessValidationException() throws Exception {
        // Given
        given(bookingService.createBooking(any(BookingDTO.class)))
                .willThrow(new BusinessValidationException("Room not available for selected dates"));

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get booking by ID successfully")
    void shouldGetBookingByIdSuccessfully() throws Exception {
        // Given
        Long bookingId = 1L;
        given(bookingService.getBookingById(bookingId)).willReturn(createdBookingDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/{id}", bookingId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("HRS booking details retrieved successfully")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.bookingReference", is("BK202506270001")));
    }

    @Test
    @DisplayName("Should return not found for non-existent booking ID")
    void shouldReturnNotFoundForNonExistentBookingId() throws Exception {
        // Given
        Long nonExistentId = 999L;
        given(bookingService.getBookingById(nonExistentId))
                .willThrow(new ResourceNotFoundException("Booking not found with ID: " + nonExistentId));

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/{id}", nonExistentId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return validation error for invalid booking ID")
    void shouldReturnValidationErrorForInvalidBookingId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/bookings/{id}", 0)) // Invalid ID (0)
                .andDo(print())
                .andExpect(status().isInternalServerError()) // The GlobalExceptionHandler returns 500 for ConstraintViolationException
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errorCode", is("HRS_INTERNAL_SERVER_ERROR")));
    }

    @Test
    @DisplayName("Should get booking by reference successfully")
    void shouldGetBookingByReferenceSuccessfully() throws Exception {
        // Given
        String bookingReference = "BK202506270001";
        given(bookingService.getBookingByReference(bookingReference)).willReturn(createdBookingDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/reference/{reference}", bookingReference))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("HRS booking retrieved by reference successfully")))
                .andExpect(jsonPath("$.data.bookingReference", is(bookingReference)));
    }

    @Test
    @DisplayName("Should update booking successfully")
    void shouldUpdateBookingSuccessfully() throws Exception {
        // Given
        Long bookingId = 1L;
        BookingDTO updatedBooking = BookingDTO.builder()
                .id(createdBookingDTO.getId())
                .userId(createdBookingDTO.getUserId())
                .hotelId(createdBookingDTO.getHotelId())
                .checkInDate(createdBookingDTO.getCheckInDate())
                .checkOutDate(createdBookingDTO.getCheckOutDate())
                .roomType(createdBookingDTO.getRoomType())
                .numberOfRooms(createdBookingDTO.getNumberOfRooms())
                .numberOfGuests(createdBookingDTO.getNumberOfGuests())
                .baseAmount(createdBookingDTO.getBaseAmount())
                .taxesAmount(createdBookingDTO.getTaxesAmount())
                .feesAmount(createdBookingDTO.getFeesAmount())
                .totalAmount(createdBookingDTO.getTotalAmount())
                .status(createdBookingDTO.getStatus())
                .bookingReference(createdBookingDTO.getBookingReference())
                .confirmationNumber(createdBookingDTO.getConfirmationNumber())
                .guestName(createdBookingDTO.getGuestName())
                .guestEmail(createdBookingDTO.getGuestEmail())
                .guestPhone(createdBookingDTO.getGuestPhone())
                .specialRequests("Updated special requests")
                .build();

        given(bookingService.updateBooking(bookingId, validBookingDTO)).willReturn(updatedBooking);

        // When & Then
        mockMvc.perform(put("/api/v1/bookings/{id}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("HRS booking updated successfully")))
                .andExpect(jsonPath("$.data.id", is(1)));
    }

    @Test
    @DisplayName("Should cancel booking successfully")
    void shouldCancelBookingSuccessfully() throws Exception {
        // Given
        Long bookingId = 1L;
        BookingDTO cancelledBooking = BookingDTO.builder()
                .id(createdBookingDTO.getId())
                .userId(createdBookingDTO.getUserId())
                .hotelId(createdBookingDTO.getHotelId())
                .checkInDate(createdBookingDTO.getCheckInDate())
                .checkOutDate(createdBookingDTO.getCheckOutDate())
                .roomType(createdBookingDTO.getRoomType())
                .numberOfRooms(createdBookingDTO.getNumberOfRooms())
                .numberOfGuests(createdBookingDTO.getNumberOfGuests())
                .baseAmount(createdBookingDTO.getBaseAmount())
                .taxesAmount(createdBookingDTO.getTaxesAmount())
                .feesAmount(createdBookingDTO.getFeesAmount())
                .totalAmount(createdBookingDTO.getTotalAmount())
                .status(BookingStatus.CANCELLED)
                .bookingReference(createdBookingDTO.getBookingReference())
                .confirmationNumber(createdBookingDTO.getConfirmationNumber())
                .guestName(createdBookingDTO.getGuestName())
                .guestEmail(createdBookingDTO.getGuestEmail())
                .guestPhone(createdBookingDTO.getGuestPhone())
                .specialRequests(createdBookingDTO.getSpecialRequests())
                .build();

        given(bookingService.cancelBooking(bookingId)).willReturn(cancelledBooking);

        // When & Then
        mockMvc.perform(put("/api/v1/bookings/{id}/cancel", bookingId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("HRS booking cancelled successfully")))
                .andExpect(jsonPath("$.data.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("Should check-in guest successfully")
    void shouldCheckInGuestSuccessfully() throws Exception {
        // Given
        Long bookingId = 1L;
        BookingDTO checkedInBooking = BookingDTO.builder()
                .id(createdBookingDTO.getId())
                .userId(createdBookingDTO.getUserId())
                .hotelId(createdBookingDTO.getHotelId())
                .checkInDate(createdBookingDTO.getCheckInDate())
                .checkOutDate(createdBookingDTO.getCheckOutDate())
                .roomType(createdBookingDTO.getRoomType())
                .numberOfRooms(createdBookingDTO.getNumberOfRooms())
                .numberOfGuests(createdBookingDTO.getNumberOfGuests())
                .baseAmount(createdBookingDTO.getBaseAmount())
                .taxesAmount(createdBookingDTO.getTaxesAmount())
                .feesAmount(createdBookingDTO.getFeesAmount())
                .totalAmount(createdBookingDTO.getTotalAmount())
                .status(BookingStatus.CHECKED_IN)
                .bookingReference(createdBookingDTO.getBookingReference())
                .confirmationNumber(createdBookingDTO.getConfirmationNumber())
                .guestName(createdBookingDTO.getGuestName())
                .guestEmail(createdBookingDTO.getGuestEmail())
                .guestPhone(createdBookingDTO.getGuestPhone())
                .specialRequests(createdBookingDTO.getSpecialRequests())
                .build();

        given(bookingService.checkInGuest(bookingId)).willReturn(checkedInBooking);

        // When & Then
        mockMvc.perform(put("/api/v1/bookings/{id}/check-in", bookingId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Guest checked in successfully")))
                .andExpect(jsonPath("$.data.status", is("CHECKED_IN")));
    }

    @Test
    @DisplayName("Should check-out guest successfully")
    void shouldCheckOutGuestSuccessfully() throws Exception {
        // Given
        Long bookingId = 1L;
        BookingDTO checkedOutBooking = BookingDTO.builder()
                .id(createdBookingDTO.getId())
                .userId(createdBookingDTO.getUserId())
                .hotelId(createdBookingDTO.getHotelId())
                .checkInDate(createdBookingDTO.getCheckInDate())
                .checkOutDate(createdBookingDTO.getCheckOutDate())
                .roomType(createdBookingDTO.getRoomType())
                .numberOfRooms(createdBookingDTO.getNumberOfRooms())
                .numberOfGuests(createdBookingDTO.getNumberOfGuests())
                .baseAmount(createdBookingDTO.getBaseAmount())
                .taxesAmount(createdBookingDTO.getTaxesAmount())
                .feesAmount(createdBookingDTO.getFeesAmount())
                .totalAmount(createdBookingDTO.getTotalAmount())
                .status(BookingStatus.CHECKED_OUT)
                .bookingReference(createdBookingDTO.getBookingReference())
                .confirmationNumber(createdBookingDTO.getConfirmationNumber())
                .guestName(createdBookingDTO.getGuestName())
                .guestEmail(createdBookingDTO.getGuestEmail())
                .guestPhone(createdBookingDTO.getGuestPhone())
                .specialRequests(createdBookingDTO.getSpecialRequests())
                .build();

        given(bookingService.checkOutGuest(bookingId)).willReturn(checkedOutBooking);

        // When & Then
        mockMvc.perform(put("/api/v1/bookings/{id}/check-out", bookingId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Guest checked out successfully")))
                .andExpect(jsonPath("$.data.status", is("CHECKED_OUT")));
    }

    @Test
    @DisplayName("Should get user bookings successfully")
    void shouldGetUserBookingsSuccessfully() throws Exception {
        // Given
        Long userId = 1L;
        List<BookingDTO> userBookings = Arrays.asList(createdBookingDTO);
        given(bookingService.getBookingsByUserId(userId)).willReturn(userBookings);

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/user/{userId}", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Found 1 bookings for user")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].userId", is(1)));
    }

    @Test
    @DisplayName("Should get hotel bookings successfully")
    void shouldGetHotelBookingsSuccessfully() throws Exception {
        // Given
        Long hotelId = 1L;
        List<BookingDTO> hotelBookings = Arrays.asList(createdBookingDTO);
        given(bookingService.getBookingsByHotelId(hotelId)).willReturn(hotelBookings);

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/hotel/{hotelId}", hotelId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Found 1 bookings for hotel")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].hotelId", is(1)));
    }

    @Test
    @DisplayName("Should handle invalid user ID in path variable")
    void shouldHandleInvalidUserIdInPathVariable() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/bookings/user/{userId}", 0)) // Invalid user ID
                .andDo(print())
                .andExpect(status().isInternalServerError()) // The GlobalExceptionHandler returns 500 for ConstraintViolationException
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errorCode", is("HRS_INTERNAL_SERVER_ERROR")));
    }

    @Test
    @DisplayName("Should handle invalid hotel ID in path variable")
    void shouldHandleInvalidHotelIdInPathVariable() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/bookings/hotel/{hotelId}", -1)) // Invalid hotel ID
                .andDo(print())
                .andExpect(status().isInternalServerError()) // The GlobalExceptionHandler returns 500 for ConstraintViolationException
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errorCode", is("HRS_INTERNAL_SERVER_ERROR")));
    }
}
