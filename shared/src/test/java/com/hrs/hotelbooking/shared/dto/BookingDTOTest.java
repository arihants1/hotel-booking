package com.hrs.hotelbooking.shared.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BookingDTO Tests")
class BookingDTOTest {

    private Validator validator;
    private BookingDTO validBookingDTO;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }

        validBookingDTO = BookingDTO.builder()
                .id(1L)
                .userId(1L)
                .hotelId(1L)
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .numberOfRooms(1)
                .numberOfGuests(2)
                .totalAmount(BigDecimal.valueOf(299.99))
                .status(BookingStatus.CONFIRMED)
                .specialRequests("Non-smoking room")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create BookingDTO using builder pattern")
        void shouldCreateBookingDTOUsingBuilder() {
            BookingDTO bookingDTO = BookingDTO.builder()
                    .id(1L)
                    .userId(1L)
                    .hotelId(1L)
                    .checkInDate(LocalDate.now().plusDays(1))
                    .checkOutDate(LocalDate.now().plusDays(3))
                    .numberOfRooms(1)
                    .numberOfGuests(2)
                    .totalAmount(BigDecimal.valueOf(299.99))
                    .status(BookingStatus.CONFIRMED)
                    .build();

            assertNotNull(bookingDTO);
            assertEquals(1L, bookingDTO.getId());
            assertEquals(1L, bookingDTO.getUserId());
            assertEquals(1L, bookingDTO.getHotelId());
            assertEquals(1, bookingDTO.getNumberOfRooms());
            assertEquals(2, bookingDTO.getNumberOfGuests());
            assertEquals(BigDecimal.valueOf(299.99), bookingDTO.getTotalAmount());
            assertEquals(BookingStatus.CONFIRMED, bookingDTO.getStatus());
        }

        @Test
        @DisplayName("Should create BookingDTO with minimal required fields")
        void shouldCreateBookingDTOWithMinimalFields() {
            BookingDTO bookingDTO = BookingDTO.builder()
                    .userId(1L)
                    .hotelId(1L)
                    .checkInDate(LocalDate.now().plusDays(1))
                    .checkOutDate(LocalDate.now().plusDays(3))
                    .numberOfGuests(1)
                    .totalAmount(BigDecimal.valueOf(100.00))
                    .status(BookingStatus.PENDING)
                    .build();

            assertNotNull(bookingDTO);
            assertNull(bookingDTO.getId());
            assertEquals(1L, bookingDTO.getUserId());
            assertEquals(1L, bookingDTO.getHotelId());
            assertEquals(1, bookingDTO.getNumberOfGuests());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation for valid BookingDTO")
        void shouldPassValidationForValidBookingDTO() {
            Set<ConstraintViolation<BookingDTO>> violations = validator.validate(validBookingDTO);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when userId is null")
        void shouldFailValidationWhenUserIdIsNull() {
            BookingDTO bookingDTO = validBookingDTO.toBuilder()
                    .userId(null)
                    .build();

            Set<ConstraintViolation<BookingDTO>> violations = validator.validate(bookingDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("User ID is required")));
        }

        @Test
        @DisplayName("Should fail validation when hotelId is null")
        void shouldFailValidationWhenHotelIdIsNull() {
            BookingDTO bookingDTO = validBookingDTO.toBuilder()
                    .hotelId(null)
                    .build();

            Set<ConstraintViolation<BookingDTO>> violations = validator.validate(bookingDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Hotel ID is required")));
        }

        @Test
        @DisplayName("Should fail validation when checkInDate is null")
        void shouldFailValidationWhenCheckInDateIsNull() {
            BookingDTO bookingDTO = validBookingDTO.toBuilder()
                    .checkInDate(null)
                    .build();

            Set<ConstraintViolation<BookingDTO>> violations = validator.validate(bookingDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Check-in date is required")));
        }

        @Test
        @DisplayName("Should fail validation when checkOutDate is null")
        void shouldFailValidationWhenCheckOutDateIsNull() {
            BookingDTO bookingDTO = validBookingDTO.toBuilder()
                    .checkOutDate(null)
                    .build();

            Set<ConstraintViolation<BookingDTO>> violations = validator.validate(bookingDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Check-out date is required")));
        }

        @Test
        @DisplayName("Should fail validation when userId is negative")
        void shouldFailValidationWhenUserIdIsNegative() {
            BookingDTO bookingDTO = validBookingDTO.toBuilder()
                    .userId(-1L)
                    .build();

            Set<ConstraintViolation<BookingDTO>> violations = validator.validate(bookingDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("must be positive")));
        }

        @Test
        @DisplayName("Should fail validation when numberOfGuests is zero")
        void shouldFailValidationWhenNumberOfGuestsIsZero() {
            BookingDTO bookingDTO = validBookingDTO.toBuilder()
                    .numberOfGuests(0)
                    .build();

            Set<ConstraintViolation<BookingDTO>> violations = validator.validate(bookingDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Number of guests must be at least 1")));
        }

        @Test
        @DisplayName("Should fail validation when totalAmount is negative")
        void shouldFailValidationWhenTotalAmountIsNegative() {
            BookingDTO bookingDTO = validBookingDTO.toBuilder()
                    .totalAmount(BigDecimal.valueOf(-100.00))
                    .build();

            Set<ConstraintViolation<BookingDTO>> violations = validator.validate(bookingDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Total amount must be greater than 0")));
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when all fields are same")
        void shouldBeEqualWhenAllFieldsAreSame() {
            BookingDTO bookingDTO1 = BookingDTO.builder()
                    .id(1L)
                    .userId(1L)
                    .hotelId(1L)
                    .checkInDate(LocalDate.of(2025, 7, 15))
                    .checkOutDate(LocalDate.of(2025, 7, 17))
                    .numberOfGuests(2)
                    .totalAmount(BigDecimal.valueOf(299.99))
                    .status(BookingStatus.CONFIRMED)
                    .build();

            BookingDTO bookingDTO2 = BookingDTO.builder()
                    .id(1L)
                    .userId(1L)
                    .hotelId(1L)
                    .checkInDate(LocalDate.of(2025, 7, 15))
                    .checkOutDate(LocalDate.of(2025, 7, 17))
                    .numberOfGuests(2)
                    .totalAmount(BigDecimal.valueOf(299.99))
                    .status(BookingStatus.CONFIRMED)
                    .build();

            assertEquals(bookingDTO1, bookingDTO2);
            assertEquals(bookingDTO1.hashCode(), bookingDTO2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when IDs are different")
        void shouldNotBeEqualWhenIdsAreDifferent() {
            BookingDTO bookingDTO1 = validBookingDTO.toBuilder().id(1L).build();
            BookingDTO bookingDTO2 = validBookingDTO.toBuilder().id(2L).build();

            assertNotEquals(bookingDTO1, bookingDTO2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            assertNotEquals(null, validBookingDTO);
        }

        @Test
        @DisplayName("Should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            assertNotEquals("not a booking", validBookingDTO);
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should contain all field names in toString")
        void shouldContainAllFieldNamesInToString() {
            String toStringResult = validBookingDTO.toString();

            assertTrue(toStringResult.contains("BookingDTO"));
            assertTrue(toStringResult.contains("id"));
            assertTrue(toStringResult.contains("userId"));
            assertTrue(toStringResult.contains("hotelId"));
            assertTrue(toStringResult.contains("checkInDate"));
            assertTrue(toStringResult.contains("checkOutDate"));
            assertTrue(toStringResult.contains("numberOfGuests"));
            assertTrue(toStringResult.contains("totalAmount"));
            assertTrue(toStringResult.contains("status"));
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should implement Serializable")
        void shouldImplementSerializable() {
            assertInstanceOf(java.io.Serializable.class, validBookingDTO);
        }
    }
}