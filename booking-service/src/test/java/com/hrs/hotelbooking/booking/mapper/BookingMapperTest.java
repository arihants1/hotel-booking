package com.hrs.hotelbooking.booking.mapper;

import com.hrs.hotelbooking.booking.entity.Booking;
import com.hrs.hotelbooking.shared.dto.BookingDTO;
import com.hrs.hotelbooking.shared.dto.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test cases for BookingMapper
 * Tests entity-to-DTO and DTO-to-entity conversion scenarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Booking Mapper Tests")
class BookingMapperTest {

    @InjectMocks
    private BookingMapper bookingMapper;

    private Booking bookingEntity;
    private BookingDTO bookingDTO;

    @BeforeEach
    void setUp() {
        bookingEntity = Booking.builder()
                .id(1L)
                .userId(1L)
                .hotelId(1L)
                .checkInDate(LocalDate.of(2025, 7, 1))
                .checkOutDate(LocalDate.of(2025, 7, 3))
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
                .specialRequests("Late check-in")
                .guestName("John Doe")
                .guestEmail("john.doe@example.com")
                .guestPhone("+1234567890")
                .paymentStatus("PENDING")
                .createdAt(LocalDateTime.of(2025, 6, 27, 10, 0))
                .updatedAt(LocalDateTime.of(2025, 6, 27, 10, 0))
                .build();

        bookingDTO = BookingDTO.builder()
                .id(1L)
                .userId(1L)
                .hotelId(1L)
                .checkInDate(LocalDate.of(2025, 7, 1))
                .checkOutDate(LocalDate.of(2025, 7, 3))
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
                .specialRequests("Late check-in")
                .guestName("John Doe")
                .guestEmail("john.doe@example.com")
                .guestPhone("+1234567890")
                .build();
    }

    @Test
    @DisplayName("Should convert booking entity to DTO successfully")
    void shouldConvertBookingEntityToDtoSuccessfully() {
        // When
        BookingDTO result = bookingMapper.toDto(bookingEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(bookingEntity.getId());
        assertThat(result.getUserId()).isEqualTo(bookingEntity.getUserId());
        assertThat(result.getHotelId()).isEqualTo(bookingEntity.getHotelId());
        assertThat(result.getCheckInDate()).isEqualTo(bookingEntity.getCheckInDate());
        assertThat(result.getCheckOutDate()).isEqualTo(bookingEntity.getCheckOutDate());
        assertThat(result.getRoomType()).isEqualTo(bookingEntity.getRoomType());
        assertThat(result.getNumberOfRooms()).isEqualTo(bookingEntity.getNumberOfRooms());
        assertThat(result.getNumberOfGuests()).isEqualTo(bookingEntity.getNumberOfGuests());
        assertThat(result.getBaseAmount()).isEqualTo(bookingEntity.getBaseAmount());
        assertThat(result.getTaxesAmount()).isEqualTo(bookingEntity.getTaxesAmount());
        assertThat(result.getFeesAmount()).isEqualTo(bookingEntity.getFeesAmount());
        assertThat(result.getTotalAmount()).isEqualTo(bookingEntity.getTotalAmount());
        assertThat(result.getStatus()).isEqualTo(bookingEntity.getStatus());
        assertThat(result.getBookingReference()).isEqualTo(bookingEntity.getBookingReference());
        assertThat(result.getConfirmationNumber()).isEqualTo(bookingEntity.getConfirmationNumber());
        assertThat(result.getSpecialRequests()).isEqualTo(bookingEntity.getSpecialRequests());
        assertThat(result.getGuestName()).isEqualTo(bookingEntity.getGuestName());
        assertThat(result.getGuestEmail()).isEqualTo(bookingEntity.getGuestEmail());
        assertThat(result.getGuestPhone()).isEqualTo(bookingEntity.getGuestPhone());
    }

    @Test
    @DisplayName("Should return null when converting null entity to DTO")
    void shouldReturnNullWhenConvertingNullEntityToDto() {
        // When
        BookingDTO result = bookingMapper.toDto(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle entity with null fields when converting to DTO")
    void shouldHandleEntityWithNullFieldsWhenConvertingToDto() {
        // Given
        Booking entityWithNullFields = Booking.builder()
                .id(1L)
                .userId(1L)
                .hotelId(1L)
                .checkInDate(LocalDate.of(2025, 7, 1))
                .checkOutDate(LocalDate.of(2025, 7, 3))
                .roomType("STANDARD")
                .numberOfRooms(1)
                .numberOfGuests(2)
                .baseAmount(new BigDecimal("200.00"))
                .status(BookingStatus.CONFIRMED)
                // Null fields intentionally omitted
                .build();

        // When
        BookingDTO result = bookingMapper.toDto(entityWithNullFields);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getHotelId()).isEqualTo(1L);
        assertThat(result.getTaxesAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getFeesAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getTotalAmount()).isNull();
        assertThat(result.getBookingReference()).isNull();
        assertThat(result.getConfirmationNumber()).isNull();
        assertThat(result.getSpecialRequests()).isNull();
        assertThat(result.getGuestName()).isNull();
        assertThat(result.getGuestEmail()).isNull();
        assertThat(result.getGuestPhone()).isNull();
    }

    @Test
    @DisplayName("Should convert booking DTO to entity successfully")
    void shouldConvertBookingDtoToEntitySuccessfully() {
        // When
        Booking result = bookingMapper.toEntity(bookingDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(bookingDTO.getId());
        assertThat(result.getUserId()).isEqualTo(bookingDTO.getUserId());
        assertThat(result.getHotelId()).isEqualTo(bookingDTO.getHotelId());
        assertThat(result.getCheckInDate()).isEqualTo(bookingDTO.getCheckInDate());
        assertThat(result.getCheckOutDate()).isEqualTo(bookingDTO.getCheckOutDate());
        assertThat(result.getRoomType()).isEqualTo(bookingDTO.getRoomType());
        assertThat(result.getNumberOfRooms()).isEqualTo(bookingDTO.getNumberOfRooms());
        assertThat(result.getNumberOfGuests()).isEqualTo(bookingDTO.getNumberOfGuests());
        assertThat(result.getBaseAmount()).isEqualTo(bookingDTO.getBaseAmount());
        assertThat(result.getTaxesAmount()).isEqualTo(bookingDTO.getTaxesAmount());
        assertThat(result.getFeesAmount()).isEqualTo(bookingDTO.getFeesAmount());
        assertThat(result.getTotalAmount()).isEqualTo(bookingDTO.getTotalAmount());
        assertThat(result.getStatus()).isEqualTo(bookingDTO.getStatus());
        assertThat(result.getBookingReference()).isEqualTo(bookingDTO.getBookingReference());
        assertThat(result.getConfirmationNumber()).isEqualTo(bookingDTO.getConfirmationNumber());
        assertThat(result.getSpecialRequests()).isEqualTo(bookingDTO.getSpecialRequests());
        assertThat(result.getGuestName()).isEqualTo(bookingDTO.getGuestName());
        assertThat(result.getGuestEmail()).isEqualTo(bookingDTO.getGuestEmail());
        assertThat(result.getGuestPhone()).isEqualTo(bookingDTO.getGuestPhone());
    }

    @Test
    @DisplayName("Should return null when converting null DTO to entity")
    void shouldReturnNullWhenConvertingNullDtoToEntity() {
        // When
        Booking result = bookingMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle DTO with null fields when converting to entity")
    void shouldHandleDtoWithNullFieldsWhenConvertingToEntity() {
        // Given
        BookingDTO dtoWithNullFields = BookingDTO.builder()
                .id(1L)
                .userId(1L)
                .hotelId(1L)
                .checkInDate(LocalDate.of(2025, 7, 1))
                .checkOutDate(LocalDate.of(2025, 7, 3))
                .roomType("STANDARD")
                .numberOfRooms(1)
                .numberOfGuests(2)
                .baseAmount(new BigDecimal("200.00"))
                .status(BookingStatus.CONFIRMED)
                // Null fields intentionally omitted
                .build();

        // When
        Booking result = bookingMapper.toEntity(dtoWithNullFields);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getHotelId()).isEqualTo(1L);
        assertThat(result.getTaxesAmount()).isNull();
        assertThat(result.getFeesAmount()).isNull();
        assertThat(result.getTotalAmount()).isNull();
        assertThat(result.getBookingReference()).isNull();
        assertThat(result.getConfirmationNumber()).isNull();
        assertThat(result.getSpecialRequests()).isNull();
        assertThat(result.getGuestName()).isNull();
        assertThat(result.getGuestEmail()).isNull();
        assertThat(result.getGuestPhone()).isNull();
    }

    @Test
    @DisplayName("Should convert list of entities to DTOs successfully")
    void shouldConvertListOfEntitiesToDtosSuccessfully() {
        // Given
        Booking secondEntity = Booking.builder()
                .id(2L)
                .userId(2L)
                .hotelId(bookingEntity.getHotelId())
                .checkInDate(bookingEntity.getCheckInDate())
                .checkOutDate(bookingEntity.getCheckOutDate())
                .roomType(bookingEntity.getRoomType())
                .numberOfRooms(bookingEntity.getNumberOfRooms())
                .numberOfGuests(bookingEntity.getNumberOfGuests())
                .baseAmount(bookingEntity.getBaseAmount())
                .taxesAmount(bookingEntity.getTaxesAmount())
                .feesAmount(bookingEntity.getFeesAmount())
                .totalAmount(bookingEntity.getTotalAmount())
                .status(bookingEntity.getStatus())
                .bookingReference("BK202506270002")
                .confirmationNumber("CNF987654321")
                .guestName(bookingEntity.getGuestName())
                .guestEmail(bookingEntity.getGuestEmail())
                .guestPhone(bookingEntity.getGuestPhone())
                .specialRequests(bookingEntity.getSpecialRequests())
                .paymentStatus(bookingEntity.getPaymentStatus())
                .build();

        List<Booking> entities = Arrays.asList(bookingEntity, secondEntity);

        // When
        List<BookingDTO> result = bookingMapper.toDtoList(entities);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        BookingDTO firstDto = result.get(0);
        assertThat(firstDto.getId()).isEqualTo(1L);
        assertThat(firstDto.getUserId()).isEqualTo(1L);
        assertThat(firstDto.getBookingReference()).isEqualTo("BK202506270001");

        BookingDTO secondDto = result.get(1);
        assertThat(secondDto.getId()).isEqualTo(2L);
        assertThat(secondDto.getUserId()).isEqualTo(2L);
        assertThat(secondDto.getBookingReference()).isEqualTo("BK202506270002");
    }

    @Test
    @DisplayName("Should return empty list when converting empty list of entities")
    void shouldReturnEmptyListWhenConvertingEmptyListOfEntities() {
        // Given
        List<Booking> emptyList = Arrays.asList();

        // When
        List<BookingDTO> result = bookingMapper.toDtoList(emptyList);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle null list when converting entities to DTOs")
    void shouldHandleNullListWhenConvertingEntitiesToDtos() {
        // When
        List<BookingDTO> result = bookingMapper.toDtoList(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should update existing entity from DTO successfully")
    void shouldUpdateExistingEntityFromDtoSuccessfully() {
        // Given
        Booking existingEntity = Booking.builder()
                .id(bookingEntity.getId())
                .userId(bookingEntity.getUserId())
                .hotelId(bookingEntity.getHotelId())
                .checkInDate(bookingEntity.getCheckInDate())
                .checkOutDate(bookingEntity.getCheckOutDate())
                .roomType(bookingEntity.getRoomType())
                .numberOfRooms(bookingEntity.getNumberOfRooms())
                .numberOfGuests(bookingEntity.getNumberOfGuests())
                .baseAmount(bookingEntity.getBaseAmount())
                .taxesAmount(bookingEntity.getTaxesAmount())
                .feesAmount(bookingEntity.getFeesAmount())
                .totalAmount(bookingEntity.getTotalAmount())
                .status(bookingEntity.getStatus())
                .bookingReference(bookingEntity.getBookingReference())
                .confirmationNumber(bookingEntity.getConfirmationNumber())
                .guestName(bookingEntity.getGuestName())
                .guestEmail(bookingEntity.getGuestEmail())
                .guestPhone(bookingEntity.getGuestPhone())
                .specialRequests("Original special requests")
                .paymentStatus(bookingEntity.getPaymentStatus())
                .build();

        BookingDTO updateDto = BookingDTO.builder()
                .specialRequests("Updated special requests")
                .guestName("Updated Guest Name")
                .numberOfGuests(3)
                .build();

        // When
        bookingMapper.updateEntityFromDto(existingEntity, updateDto);

        // Then
        assertThat(existingEntity.getSpecialRequests()).isEqualTo("Updated special requests");
        assertThat(existingEntity.getGuestName()).isEqualTo("Updated Guest Name");
        assertThat(existingEntity.getNumberOfGuests()).isEqualTo(3);
        // Original values should remain unchanged for fields not in the update DTO
        assertThat(existingEntity.getId()).isEqualTo(bookingEntity.getId());
        assertThat(existingEntity.getBookingReference()).isEqualTo(bookingEntity.getBookingReference());
    }

    @Test
    @DisplayName("Should preserve decimal precision in amount fields")
    void shouldPreserveDecimalPrecisionInAmountFields() {
        // Given
        Booking entityWithPreciseAmounts = Booking.builder()
                .id(bookingEntity.getId())
                .userId(bookingEntity.getUserId())
                .hotelId(bookingEntity.getHotelId())
                .checkInDate(bookingEntity.getCheckInDate())
                .checkOutDate(bookingEntity.getCheckOutDate())
                .roomType(bookingEntity.getRoomType())
                .numberOfRooms(bookingEntity.getNumberOfRooms())
                .numberOfGuests(bookingEntity.getNumberOfGuests())
                .baseAmount(new BigDecimal("199.99"))
                .taxesAmount(new BigDecimal("19.999"))
                .feesAmount(new BigDecimal("9.995"))
                .totalAmount(new BigDecimal("229.984"))
                .status(bookingEntity.getStatus())
                .bookingReference(bookingEntity.getBookingReference())
                .confirmationNumber(bookingEntity.getConfirmationNumber())
                .guestName(bookingEntity.getGuestName())
                .guestEmail(bookingEntity.getGuestEmail())
                .guestPhone(bookingEntity.getGuestPhone())
                .specialRequests(bookingEntity.getSpecialRequests())
                .paymentStatus(bookingEntity.getPaymentStatus())
                .build();

        // When
        BookingDTO result = bookingMapper.toDto(entityWithPreciseAmounts);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBaseAmount()).isEqualTo(new BigDecimal("199.99"));
        assertThat(result.getTaxesAmount()).isEqualTo(new BigDecimal("19.999"));
        assertThat(result.getFeesAmount()).isEqualTo(new BigDecimal("9.995"));
        assertThat(result.getTotalAmount()).isEqualTo(new BigDecimal("229.984"));
    }

    @Test
    @DisplayName("Should handle all booking statuses correctly")
    void shouldHandleAllBookingStatusesCorrectly() {
        // Test each booking status
        BookingStatus[] statuses = BookingStatus.values();

        for (BookingStatus status : statuses) {
            // Given
            Booking entityWithStatus = Booking.builder()
                    .id(bookingEntity.getId())
                    .userId(bookingEntity.getUserId())
                    .hotelId(bookingEntity.getHotelId())
                    .checkInDate(bookingEntity.getCheckInDate())
                    .checkOutDate(bookingEntity.getCheckOutDate())
                    .roomType(bookingEntity.getRoomType())
                    .numberOfRooms(bookingEntity.getNumberOfRooms())
                    .numberOfGuests(bookingEntity.getNumberOfGuests())
                    .baseAmount(bookingEntity.getBaseAmount())
                    .taxesAmount(bookingEntity.getTaxesAmount())
                    .feesAmount(bookingEntity.getFeesAmount())
                    .totalAmount(bookingEntity.getTotalAmount())
                    .status(status)
                    .bookingReference(bookingEntity.getBookingReference())
                    .confirmationNumber(bookingEntity.getConfirmationNumber())
                    .guestName(bookingEntity.getGuestName())
                    .guestEmail(bookingEntity.getGuestEmail())
                    .guestPhone(bookingEntity.getGuestPhone())
                    .specialRequests(bookingEntity.getSpecialRequests())
                    .paymentStatus(bookingEntity.getPaymentStatus())
                    .build();

            // When
            BookingDTO result = bookingMapper.toDto(entityWithStatus);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(status);
        }
    }

    @Test
    @DisplayName("Should handle date conversion correctly")
    void shouldHandleDateConversionCorrectly() {
        // Given
        LocalDate checkIn = LocalDate.of(2025, 12, 25);
        LocalDate checkOut = LocalDate.of(2025, 12, 27);

        Booking entityWithDates = Booking.builder()
                .id(bookingEntity.getId())
                .userId(bookingEntity.getUserId())
                .hotelId(bookingEntity.getHotelId())
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomType(bookingEntity.getRoomType())
                .numberOfRooms(bookingEntity.getNumberOfRooms())
                .numberOfGuests(bookingEntity.getNumberOfGuests())
                .baseAmount(bookingEntity.getBaseAmount())
                .taxesAmount(bookingEntity.getTaxesAmount())
                .feesAmount(bookingEntity.getFeesAmount())
                .totalAmount(bookingEntity.getTotalAmount())
                .status(bookingEntity.getStatus())
                .bookingReference(bookingEntity.getBookingReference())
                .confirmationNumber(bookingEntity.getConfirmationNumber())
                .guestName(bookingEntity.getGuestName())
                .guestEmail(bookingEntity.getGuestEmail())
                .guestPhone(bookingEntity.getGuestPhone())
                .specialRequests(bookingEntity.getSpecialRequests())
                .paymentStatus(bookingEntity.getPaymentStatus())
                .build();

        // When
        BookingDTO result = bookingMapper.toDto(entityWithDates);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCheckInDate()).isEqualTo(checkIn);
        assertThat(result.getCheckOutDate()).isEqualTo(checkOut);
    }

    @Test
    @DisplayName("Should handle special characters in text fields")
    void shouldHandleSpecialCharactersInTextFields() {
        // Given
        Booking entityWithSpecialChars = Booking.builder()
                .id(bookingEntity.getId())
                .userId(bookingEntity.getUserId())
                .hotelId(bookingEntity.getHotelId())
                .checkInDate(bookingEntity.getCheckInDate())
                .checkOutDate(bookingEntity.getCheckOutDate())
                .roomType(bookingEntity.getRoomType())
                .numberOfRooms(bookingEntity.getNumberOfRooms())
                .numberOfGuests(bookingEntity.getNumberOfGuests())
                .baseAmount(bookingEntity.getBaseAmount())
                .taxesAmount(bookingEntity.getTaxesAmount())
                .feesAmount(bookingEntity.getFeesAmount())
                .totalAmount(bookingEntity.getTotalAmount())
                .status(bookingEntity.getStatus())
                .bookingReference(bookingEntity.getBookingReference())
                .confirmationNumber(bookingEntity.getConfirmationNumber())
                .guestName("José María Rodríguez")
                .guestEmail("josé.maría@example.com")
                .guestPhone(bookingEntity.getGuestPhone())
                .specialRequests("Room with café view & WiFi")
                .paymentStatus(bookingEntity.getPaymentStatus())
                .build();

        // When
        BookingDTO result = bookingMapper.toDto(entityWithSpecialChars);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGuestName()).isEqualTo("José María Rodríguez");
        assertThat(result.getGuestEmail()).isEqualTo("josé.maría@example.com");
        assertThat(result.getSpecialRequests()).isEqualTo("Room with café view & WiFi");
    }

    @Test
    @DisplayName("Should handle bidirectional conversion consistency")
    void shouldHandleBidirectionalConversionConsistency() {
        // When - Convert entity to DTO and back to entity
        BookingDTO dto = bookingMapper.toDto(bookingEntity);
        Booking convertedEntity = bookingMapper.toEntity(dto);

        // Then - Original entity should be equivalent to converted entity (excluding timestamps)
        assertThat(convertedEntity).isNotNull();
        assertThat(convertedEntity.getId()).isEqualTo(bookingEntity.getId());
        assertThat(convertedEntity.getUserId()).isEqualTo(bookingEntity.getUserId());
        assertThat(convertedEntity.getHotelId()).isEqualTo(bookingEntity.getHotelId());
        assertThat(convertedEntity.getCheckInDate()).isEqualTo(bookingEntity.getCheckInDate());
        assertThat(convertedEntity.getCheckOutDate()).isEqualTo(bookingEntity.getCheckOutDate());
        assertThat(convertedEntity.getRoomType()).isEqualTo(bookingEntity.getRoomType());
        assertThat(convertedEntity.getNumberOfRooms()).isEqualTo(bookingEntity.getNumberOfRooms());
        assertThat(convertedEntity.getNumberOfGuests()).isEqualTo(bookingEntity.getNumberOfGuests());
        assertThat(convertedEntity.getBaseAmount()).isEqualTo(bookingEntity.getBaseAmount());
        assertThat(convertedEntity.getTaxesAmount()).isEqualTo(bookingEntity.getTaxesAmount());
        assertThat(convertedEntity.getFeesAmount()).isEqualTo(bookingEntity.getFeesAmount());
        assertThat(convertedEntity.getTotalAmount()).isEqualTo(bookingEntity.getTotalAmount());
        assertThat(convertedEntity.getStatus()).isEqualTo(bookingEntity.getStatus());
        assertThat(convertedEntity.getBookingReference()).isEqualTo(bookingEntity.getBookingReference());
        assertThat(convertedEntity.getConfirmationNumber()).isEqualTo(bookingEntity.getConfirmationNumber());
        assertThat(convertedEntity.getSpecialRequests()).isEqualTo(bookingEntity.getSpecialRequests());
        assertThat(convertedEntity.getGuestName()).isEqualTo(bookingEntity.getGuestName());
        assertThat(convertedEntity.getGuestEmail()).isEqualTo(bookingEntity.getGuestEmail());
        assertThat(convertedEntity.getGuestPhone()).isEqualTo(bookingEntity.getGuestPhone());
    }
}
