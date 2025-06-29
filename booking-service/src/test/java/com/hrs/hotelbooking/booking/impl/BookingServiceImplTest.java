package com.hrs.hotelbooking.booking.impl;

import com.hrs.hotelbooking.booking.entity.Booking;
import com.hrs.hotelbooking.booking.mapper.BookingMapper;
import com.hrs.hotelbooking.booking.repository.BookingRepository;
import com.hrs.hotelbooking.booking.service.impl.BookingServiceImpl;
import com.hrs.hotelbooking.shared.dto.BookingDTO;
import com.hrs.hotelbooking.shared.dto.BookingStatus;
import com.hrs.hotelbooking.shared.exception.BusinessValidationException;
import com.hrs.hotelbooking.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for BookingServiceImpl
 * Tests all essential booking operations including validation scenarios
 *
 * @author arihants1
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private BookingDTO validBookingDTO;
    private Booking bookingEntity;
    private BookingDTO savedBookingDTO;

    @BeforeEach
    void setUp() {
        // Setup valid booking DTO
        validBookingDTO = BookingDTO.builder()
                .userId(1L)
                .hotelId(1L)
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .numberOfGuests(2)
                .numberOfRooms(1)  // Add missing required field
                .roomType("STANDARD")
                .totalAmount(new BigDecimal("200.00"))
                .guestName("John Doe")
                .guestEmail("john.doe@example.com")
                .guestPhone("+1234567890")
                .build();

        // Setup booking entity
        bookingEntity = new Booking();
        bookingEntity.setId(1L);
        bookingEntity.setUserId(1L);
        bookingEntity.setHotelId(1L);
        bookingEntity.setCheckInDate(LocalDate.now().plusDays(1));
        bookingEntity.setCheckOutDate(LocalDate.now().plusDays(3));
        bookingEntity.setNumberOfGuests(2);
        bookingEntity.setNumberOfRooms(1);  // Add missing required field
        bookingEntity.setStatus(BookingStatus.CONFIRMED);
        bookingEntity.setBookingReference("HRS202506291234567890");
        bookingEntity.setConfirmationNumber("CONF123456789");

        // Setup saved booking DTO
        savedBookingDTO = BookingDTO.builder()
                .id(1L)
                .userId(1L)
                .hotelId(1L)
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .numberOfGuests(2)
                .numberOfRooms(1)  // Add missing required field
                .status(BookingStatus.CONFIRMED)
                .bookingReference("HRS202506291234567890")
                .confirmationNumber("CONF123456789")
                .build();
    }

    @Test
    void createBooking_ShouldReturnBookingDTO_WhenValidBookingProvided() {
        // Given
        given(bookingRepository.findDuplicateBookings(
                anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class), isNull()))
                .willReturn(Collections.emptyList());
        given(bookingMapper.toEntity(any(BookingDTO.class))).willReturn(bookingEntity);
        given(bookingRepository.save(any(Booking.class))).willReturn(bookingEntity);
        given(bookingMapper.toDto(any(Booking.class))).willReturn(savedBookingDTO);

        // When
        BookingDTO result = bookingService.createBooking(validBookingDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(result.getBookingReference()).isNotBlank();
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_ShouldThrowBusinessValidationException_WhenUserIdIsNull() {
        // Given
        validBookingDTO.setUserId(null);

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(validBookingDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Valid user ID is required");
    }

    @Test
    void createBooking_ShouldThrowBusinessValidationException_WhenHotelIdIsNull() {
        // Given
        validBookingDTO.setHotelId(null);

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(validBookingDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Valid hotel ID is required");
    }

    @Test
    void createBooking_ShouldThrowBusinessValidationException_WhenCheckInDateInPast() {
        // Given
        validBookingDTO.setCheckInDate(LocalDate.now().minusDays(1));

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(validBookingDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Check-in date cannot be in the past");
    }

    @Test
    void createBooking_ShouldThrowBusinessValidationException_WhenCheckOutBeforeCheckIn() {
        // Given
        validBookingDTO.setCheckInDate(LocalDate.now().plusDays(3));
        validBookingDTO.setCheckOutDate(LocalDate.now().plusDays(2));

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(validBookingDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Check-out date must be at least one day after check-in");
    }

    @Test
    void createBooking_ShouldThrowBusinessValidationException_WhenDuplicateBookingExists() {
        // Given
        List<Booking> duplicateBookings = List.of(bookingEntity);
        given(bookingRepository.findDuplicateBookings(
                anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class), isNull()))
                .willReturn(duplicateBookings);

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(validBookingDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("You already have a booking for this hotel with overlapping dates");
    }

    @Test
    void createBooking_ShouldThrowRuntimeException_WhenDatabaseSaveFails() {
        // Given
        given(bookingRepository.findDuplicateBookings(
                anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class), isNull()))
                .willReturn(Collections.emptyList());
        given(bookingMapper.toEntity(any(BookingDTO.class))).willReturn(bookingEntity);
        given(bookingRepository.save(any(Booking.class)))
                .willThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(validBookingDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database connection failed");
    }

    @Test
    void getBookingById_ShouldReturnBookingDTO_WhenBookingExists() {
        // Given
        Long bookingId = 1L;
        given(bookingRepository.findById(bookingId)).willReturn(Optional.of(bookingEntity));
        given(bookingMapper.toDto(bookingEntity)).willReturn(savedBookingDTO);

        // When
        BookingDTO result = bookingService.getBookingById(bookingId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(bookingId);
        verify(bookingRepository).findById(bookingId);
    }

    @Test
    void getBookingById_ShouldThrowResourceNotFoundException_WhenBookingNotExists() {
        // Given
        Long bookingId = 999L;
        given(bookingRepository.findById(bookingId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookingService.getBookingById(bookingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("HRS Booking")
                .hasMessageContaining("id")
                .hasMessageContaining("999");
    }

    @Test
    void getBookingById_ShouldThrowBusinessValidationException_WhenIdIsNull() {
        // When & Then
        assertThatThrownBy(() -> bookingService.getBookingById(null))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Invalid booking ID provided");
    }

    @Test
    void getBookingByReference_ShouldReturnBookingDTO_WhenBookingExists() {
        // Given
        String bookingReference = "HRS202506291234567890";
        given(bookingRepository.findByBookingReference(bookingReference))
                .willReturn(Optional.of(bookingEntity));
        given(bookingMapper.toDto(bookingEntity)).willReturn(savedBookingDTO);

        // When
        BookingDTO result = bookingService.getBookingByReference(bookingReference);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBookingReference()).isEqualTo(bookingReference);
        verify(bookingRepository).findByBookingReference(bookingReference);
    }

    @Test
    void getBookingByReference_ShouldThrowResourceNotFoundException_WhenBookingNotExists() {
        // Given
        String bookingReference = "INVALID_REF";
        given(bookingRepository.findByBookingReference(bookingReference))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookingService.getBookingByReference(bookingReference))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("HRS Booking")
                .hasMessageContaining("reference")
                .hasMessageContaining("INVALID_REF");
    }

    @Test
    void updateBooking_ShouldReturnUpdatedBookingDTO_WhenValidUpdateProvided() {
        // Given
        Long bookingId = 1L;
        // Mock the booking as modifiable by setting status to CONFIRMED
        bookingEntity.setStatus(BookingStatus.CONFIRMED);
        BookingDTO updatedBookingDTO = BookingDTO.builder()
                .numberOfGuests(3)
                .guestName("Jane Doe")
                .build();

        given(bookingRepository.findById(bookingId)).willReturn(Optional.of(bookingEntity));
        given(bookingRepository.save(any(Booking.class))).willReturn(bookingEntity);
        given(bookingMapper.toDto(bookingEntity)).willReturn(savedBookingDTO);

        // When
        BookingDTO result = bookingService.updateBooking(bookingId, updatedBookingDTO);

        // Then
        assertThat(result).isNotNull();
        verify(bookingMapper).updateEntityFromDto(eq(bookingEntity), eq(updatedBookingDTO));
        verify(bookingRepository).save(bookingEntity);
    }

    @Test
    void updateBooking_ShouldThrowBusinessValidationException_WhenBookingNotModifiable() {
        // Given
        Long bookingId = 1L;
        // Mock the booking as not modifiable by setting status to CANCELLED
        bookingEntity.setStatus(BookingStatus.CANCELLED);
        BookingDTO updatedBookingDTO = BookingDTO.builder()
                .numberOfGuests(3)
                .build();

        given(bookingRepository.findById(bookingId)).willReturn(Optional.of(bookingEntity));

        // When & Then
        assertThatThrownBy(() -> bookingService.updateBooking(bookingId, updatedBookingDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("cannot be modified");
    }

    @Test
    void cancelBooking_ShouldReturnCancelledBookingDTO_WhenBookingCanBeCancelled() {
        // Given
        Long bookingId = 1L;
        // Mock the booking as cancellable by setting status to CONFIRMED
        bookingEntity.setStatus(BookingStatus.CONFIRMED);
        BookingDTO cancelledBookingDTO = BookingDTO.builder()
                .id(1L)
                .userId(1L)
                .hotelId(1L)
                .status(BookingStatus.CANCELLED)
                .bookingReference("HRS202506291234567890")
                .build();

        given(bookingRepository.findById(bookingId)).willReturn(Optional.of(bookingEntity));
        given(bookingRepository.save(any(Booking.class))).willReturn(bookingEntity);
        given(bookingMapper.toDto(bookingEntity)).willReturn(cancelledBookingDTO);

        // When
        BookingDTO result = bookingService.cancelBooking(bookingId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(bookingEntity);
    }

    @Test
    void cancelBooking_ShouldThrowBusinessValidationException_WhenBookingNotCancellable() {
        // Given
        Long bookingId = 1L;
        // Mock the booking as not cancellable by setting status to CANCELLED
        bookingEntity.setStatus(BookingStatus.CANCELLED);

        given(bookingRepository.findById(bookingId)).willReturn(Optional.of(bookingEntity));

        // When & Then
        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("cannot be cancelled");
    }

    @Test
    void getBookingsByUserId_ShouldReturnListOfBookings_WhenUserHasBookings() {
        // Given
        Long userId = 1L;
        List<Booking> userBookings = List.of(bookingEntity);
        List<BookingDTO> userBookingDTOs = List.of(savedBookingDTO);

        given(bookingRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .willReturn(userBookings);
        given(bookingMapper.toDtoList(userBookings)).willReturn(userBookingDTOs);

        // When
        List<BookingDTO> result = bookingService.getBookingsByUserId(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
        verify(bookingRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void getBookingsByUserId_ShouldReturnEmptyList_WhenUserHasNoBookings() {
        // Given
        Long userId = 1L;
        given(bookingRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .willReturn(Collections.emptyList());
        given(bookingMapper.toDtoList(Collections.emptyList()))
                .willReturn(Collections.emptyList());

        // When
        List<BookingDTO> result = bookingService.getBookingsByUserId(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void getBookingsByHotelId_ShouldReturnListOfBookings_WhenHotelHasBookings() {
        // Given
        Long hotelId = 1L;
        List<Booking> hotelBookings = List.of(bookingEntity);
        List<BookingDTO> hotelBookingDTOs = List.of(savedBookingDTO);

        given(bookingRepository.findByHotelIdOrderByCheckInDateAsc(hotelId))
                .willReturn(hotelBookings);
        given(bookingMapper.toDtoList(hotelBookings)).willReturn(hotelBookingDTOs);

        // When
        List<BookingDTO> result = bookingService.getBookingsByHotelId(hotelId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHotelId()).isEqualTo(hotelId);
        verify(bookingRepository).findByHotelIdOrderByCheckInDateAsc(hotelId);
    }
}
