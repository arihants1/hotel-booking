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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HotelDTO Tests")
class HotelDTOTest {

    private Validator validator;
    private HotelDTO validHotelDTO;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }

        Map<String, Object> amenities = new HashMap<>();
        amenities.put("wifi", true);
        amenities.put("pool", true);
        amenities.put("gym", true);

        validHotelDTO = HotelDTO.builder()
                .id(1L)
                .name("HRS Grand Hotel")
                .description("Luxury hotel in city center")
                .location("123 Main Street, Downtown")
                .city("New York")
                .country("USA")
                .starRating(4)
                .amenities(amenities)
                .basePrice(BigDecimal.valueOf(199.99))
                .totalRooms(150)
                .phone("+1-555-0123")
                .email("info@hrsgrand.com")
                .website("https://hrsgrand.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .availableRooms(50)
                .build();
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create HotelDTO using builder pattern")
        void shouldCreateHotelDTOUsingBuilder() {
            HotelDTO hotelDTO = HotelDTO.builder()
                    .id(1L)
                    .name("HRS Grand Hotel")
                    .location("123 Main Street, Downtown")
                    .city("New York")
                    .country("USA")
                    .starRating(4)
                    .basePrice(BigDecimal.valueOf(199.99))
                    .totalRooms(150)
                    .build();

            assertNotNull(hotelDTO);
            assertEquals(1L, hotelDTO.getId());
            assertEquals("HRS Grand Hotel", hotelDTO.getName());
            assertEquals("123 Main Street, Downtown", hotelDTO.getLocation());
            assertEquals("New York", hotelDTO.getCity());
            assertEquals("USA", hotelDTO.getCountry());
            assertEquals(4, hotelDTO.getStarRating());
            assertEquals(BigDecimal.valueOf(199.99), hotelDTO.getBasePrice());
            assertEquals(150, hotelDTO.getTotalRooms());
        }

        @Test
        @DisplayName("Should create HotelDTO with minimal required fields")
        void shouldCreateHotelDTOWithMinimalFields() {
            HotelDTO hotelDTO = HotelDTO.builder()
                    .name("Basic Hotel")
                    .location("Downtown")
                    .city("Chicago")
                    .country("USA")
                    .build();

            assertNotNull(hotelDTO);
            assertNull(hotelDTO.getId());
            assertEquals("Basic Hotel", hotelDTO.getName());
            assertEquals("Downtown", hotelDTO.getLocation());
            assertEquals("Chicago", hotelDTO.getCity());
            assertEquals("USA", hotelDTO.getCountry());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation for valid HotelDTO")
        void shouldPassValidationForValidHotelDTO() {
            Set<ConstraintViolation<HotelDTO>> violations = validator.validate(validHotelDTO);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when name is null")
        void shouldFailValidationWhenNameIsNull() {
            HotelDTO hotelDTO = validHotelDTO.toBuilder()
                    .name(null)
                    .build();

            Set<ConstraintViolation<HotelDTO>> violations = validator.validate(hotelDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Hotel name is required")));
        }

        @Test
        @DisplayName("Should fail validation when name is blank")
        void shouldFailValidationWhenNameIsBlank() {
            HotelDTO hotelDTO = validHotelDTO.toBuilder()
                    .name("   ")
                    .build();

            Set<ConstraintViolation<HotelDTO>> violations = validator.validate(hotelDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Hotel name is required")));
        }

        @Test
        @DisplayName("Should fail validation when name exceeds max length")
        void shouldFailValidationWhenNameExceedsMaxLength() {
            String longName = "A".repeat(256);
            HotelDTO hotelDTO = validHotelDTO.toBuilder()
                    .name(longName)
                    .build();

            Set<ConstraintViolation<HotelDTO>> violations = validator.validate(hotelDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Hotel name cannot exceed 255 characters")));
        }

        @Test
        @DisplayName("Should fail validation when location is null")
        void shouldFailValidationWhenLocationIsNull() {
            HotelDTO hotelDTO = validHotelDTO.toBuilder()
                    .location(null)
                    .build();

            Set<ConstraintViolation<HotelDTO>> violations = validator.validate(hotelDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Location is required")));
        }

        @Test
        @DisplayName("Should fail validation when city is null")
        void shouldFailValidationWhenCityIsNull() {
            HotelDTO hotelDTO = validHotelDTO.toBuilder()
                    .city(null)
                    .build();

            Set<ConstraintViolation<HotelDTO>> violations = validator.validate(hotelDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("City is required")));
        }

        @Test
        @DisplayName("Should fail validation when country is null")
        void shouldFailValidationWhenCountryIsNull() {
            HotelDTO hotelDTO = validHotelDTO.toBuilder()
                    .country(null)
                    .build();

            Set<ConstraintViolation<HotelDTO>> violations = validator.validate(hotelDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Country is required")));
        }

        @Test
        @DisplayName("Should fail validation when star rating is less than 1")
        void shouldFailValidationWhenStarRatingIsLessThanOne() {
            HotelDTO hotelDTO = validHotelDTO.toBuilder()
                    .starRating(0)
                    .build();

            Set<ConstraintViolation<HotelDTO>> violations = validator.validate(hotelDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Star rating must be at least 1")));
        }

        @Test
        @DisplayName("Should fail validation when star rating exceeds 5")
        void shouldFailValidationWhenStarRatingExceedsFive() {
            HotelDTO hotelDTO = validHotelDTO.toBuilder()
                    .starRating(6)
                    .build();

            Set<ConstraintViolation<HotelDTO>> violations = validator.validate(hotelDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Star rating cannot exceed 5")));
        }

        @Test
        @DisplayName("Should fail validation when base price is negative")
        void shouldFailValidationWhenBasePriceIsNegative() {
            HotelDTO hotelDTO = validHotelDTO.toBuilder()
                    .basePrice(BigDecimal.valueOf(-100.00))
                    .build();

            Set<ConstraintViolation<HotelDTO>> violations = validator.validate(hotelDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Base price must be greater than 0")));
        }

        @Test
        @DisplayName("Should fail validation when total rooms is less than 1")
        void shouldFailValidationWhenTotalRoomsIsLessThanOne() {
            HotelDTO hotelDTO = validHotelDTO.toBuilder()
                    .totalRooms(0)
                    .build();

            Set<ConstraintViolation<HotelDTO>> violations = validator.validate(hotelDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Total rooms must be at least 1")));
        }

        @Test
        @DisplayName("Should fail validation when phone format is invalid")
        void shouldFailValidationWhenPhoneFormatIsInvalid() {
            HotelDTO hotelDTO = validHotelDTO.toBuilder()
                    .phone("invalid-phone")
                    .build();

            Set<ConstraintViolation<HotelDTO>> violations = validator.validate(hotelDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Phone must be valid")));
        }

        @Test
        @DisplayName("Should fail validation when email format is invalid")
        void shouldFailValidationWhenEmailFormatIsInvalid() {
            HotelDTO hotelDTO = validHotelDTO.toBuilder()
                    .email("invalid-email")
                    .build();

            Set<ConstraintViolation<HotelDTO>> violations = validator.validate(hotelDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Email must be valid")));
        }

        @Test
        @DisplayName("Should fail validation when description exceeds max length")
        void shouldFailValidationWhenDescriptionExceedsMaxLength() {
            String longDescription = "A".repeat(2001);
            HotelDTO hotelDTO = validHotelDTO.toBuilder()
                    .description(longDescription)
                    .build();

            Set<ConstraintViolation<HotelDTO>> violations = validator.validate(hotelDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Description cannot exceed 2000 characters")));
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when all fields are same")
        void shouldBeEqualWhenAllFieldsAreSame() {
            HotelDTO hotelDTO1 = HotelDTO.builder()
                    .id(1L)
                    .name("HRS Grand Hotel")
                    .location("123 Main Street")
                    .city("New York")
                    .country("USA")
                    .starRating(4)
                    .basePrice(BigDecimal.valueOf(199.99))
                    .totalRooms(150)
                    .build();

            HotelDTO hotelDTO2 = HotelDTO.builder()
                    .id(1L)
                    .name("HRS Grand Hotel")
                    .location("123 Main Street")
                    .city("New York")
                    .country("USA")
                    .starRating(4)
                    .basePrice(BigDecimal.valueOf(199.99))
                    .totalRooms(150)
                    .build();

            assertEquals(hotelDTO1, hotelDTO2);
            assertEquals(hotelDTO1.hashCode(), hotelDTO2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when IDs are different")
        void shouldNotBeEqualWhenIdsAreDifferent() {
            HotelDTO hotelDTO1 = validHotelDTO.toBuilder().id(1L).build();
            HotelDTO hotelDTO2 = validHotelDTO.toBuilder().id(2L).build();

            assertNotEquals(hotelDTO1, hotelDTO2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            assertNotEquals(null, validHotelDTO);
        }

        @Test
        @DisplayName("Should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            assertNotEquals("not a hotel", validHotelDTO);
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should contain all field names in toString")
        void shouldContainAllFieldNamesInToString() {
            String toStringResult = validHotelDTO.toString();

            assertTrue(toStringResult.contains("HotelDTO"));
            assertTrue(toStringResult.contains("id"));
            assertTrue(toStringResult.contains("name"));
            assertTrue(toStringResult.contains("location"));
            assertTrue(toStringResult.contains("city"));
            assertTrue(toStringResult.contains("country"));
            assertTrue(toStringResult.contains("starRating"));
            assertTrue(toStringResult.contains("basePrice"));
            assertTrue(toStringResult.contains("totalRooms"));
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should implement Serializable")
        void shouldImplementSerializable() {
            assertInstanceOf(java.io.Serializable.class, validHotelDTO);
        }
    }

    @Nested
    @DisplayName("Amenities Tests")
    class AmenitiesTests {

        @Test
        @DisplayName("Should handle null amenities")
        void shouldHandleNullAmenities() {
            HotelDTO hotelDTO = validHotelDTO.toBuilder()
                    .amenities(null)
                    .build();

            assertNotNull(hotelDTO);
            assertNull(hotelDTO.getAmenities());
        }

        @Test
        @DisplayName("Should handle empty amenities map")
        void shouldHandleEmptyAmenitiesMap() {
            HotelDTO hotelDTO = validHotelDTO.toBuilder()
                    .amenities(new HashMap<>())
                    .build();

            assertNotNull(hotelDTO);
            assertNotNull(hotelDTO.getAmenities());
            assertTrue(hotelDTO.getAmenities().isEmpty());
        }

        @Test
        @DisplayName("Should preserve amenities data")
        void shouldPreserveAmenitiesData() {
            Map<String, Object> amenities = new HashMap<>();
            amenities.put("wifi", true);
            amenities.put("pool", false);
            amenities.put("rating", 4.5);
            amenities.put("services", java.util.Arrays.asList("spa", "restaurant"));

            HotelDTO hotelDTO = validHotelDTO.toBuilder()
                    .amenities(amenities)
                    .build();

            assertNotNull(hotelDTO.getAmenities());
            assertEquals(4, hotelDTO.getAmenities().size());
            assertEquals(true, hotelDTO.getAmenities().get("wifi"));
            assertEquals(false, hotelDTO.getAmenities().get("pool"));
            assertEquals(4.5, hotelDTO.getAmenities().get("rating"));
        }
    }
}
