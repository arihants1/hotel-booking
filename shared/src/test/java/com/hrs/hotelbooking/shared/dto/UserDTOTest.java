package com.hrs.hotelbooking.shared.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserDTO Tests")
class UserDTOTest {

    private Validator validator;
    private UserDTO validUserDTO;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }

        validUserDTO = UserDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1-555-0123")
                .fullName("John Doe")
                .displayName("John Doe")
                .isActive(true)
                .hasCompleteProfile(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create UserDTO using builder pattern")
        void shouldCreateUserDTOUsingBuilder() {
            UserDTO userDTO = UserDTO.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .phone("+1-555-0123")
                    .isActive(true)
                    .build();

            assertNotNull(userDTO);
            assertEquals(1L, userDTO.getId());
            assertEquals("John", userDTO.getFirstName());
            assertEquals("Doe", userDTO.getLastName());
            assertEquals("john.doe@example.com", userDTO.getEmail());
            assertEquals("+1-555-0123", userDTO.getPhone());
            assertEquals(true, userDTO.getIsActive());
        }

        @Test
        @DisplayName("Should create UserDTO with minimal required fields")
        void shouldCreateUserDTOWithMinimalFields() {
            UserDTO userDTO = UserDTO.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .build();

            assertNotNull(userDTO);
            assertNull(userDTO.getId());
            assertEquals("Jane", userDTO.getFirstName());
            assertEquals("Smith", userDTO.getLastName());
            assertEquals("jane.smith@example.com", userDTO.getEmail());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation for valid UserDTO")
        void shouldPassValidationForValidUserDTO() {
            Set<ConstraintViolation<UserDTO>> violations = validator.validate(validUserDTO);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when firstName is null")
        void shouldFailValidationWhenFirstNameIsNull() {
            UserDTO userDTO = validUserDTO.toBuilder()
                    .firstName(null)
                    .build();

            Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("First name is required")));
        }

        @Test
        @DisplayName("Should fail validation when firstName is blank")
        void shouldFailValidationWhenFirstNameIsBlank() {
            UserDTO userDTO = validUserDTO.toBuilder()
                    .firstName("   ")
                    .build();

            Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("First name is required")));
        }

        @Test
        @DisplayName("Should fail validation when firstName exceeds max length")
        void shouldFailValidationWhenFirstNameExceedsMaxLength() {
            String longFirstName = "A".repeat(101);
            UserDTO userDTO = validUserDTO.toBuilder()
                    .firstName(longFirstName)
                    .build();

            Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("First name cannot exceed 100 characters")));
        }

        @Test
        @DisplayName("Should fail validation when lastName is null")
        void shouldFailValidationWhenLastNameIsNull() {
            UserDTO userDTO = validUserDTO.toBuilder()
                    .lastName(null)
                    .build();

            Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Last name is required")));
        }

        @Test
        @DisplayName("Should fail validation when lastName is blank")
        void shouldFailValidationWhenLastNameIsBlank() {
            UserDTO userDTO = validUserDTO.toBuilder()
                    .lastName("   ")
                    .build();

            Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Last name is required")));
        }

        @Test
        @DisplayName("Should fail validation when lastName exceeds max length")
        void shouldFailValidationWhenLastNameExceedsMaxLength() {
            String longLastName = "B".repeat(101);
            UserDTO userDTO = validUserDTO.toBuilder()
                    .lastName(longLastName)
                    .build();

            Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Last name cannot exceed 100 characters")));
        }

        @Test
        @DisplayName("Should fail validation when email is null")
        void shouldFailValidationWhenEmailIsNull() {
            UserDTO userDTO = validUserDTO.toBuilder()
                    .email(null)
                    .build();

            Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Email is required")));
        }

        @Test
        @DisplayName("Should fail validation when email is blank")
        void shouldFailValidationWhenEmailIsBlank() {
            UserDTO userDTO = validUserDTO.toBuilder()
                    .email("   ")
                    .build();

            Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Email is required")));
        }

        @Test
        @DisplayName("Should fail validation when email format is invalid")
        void shouldFailValidationWhenEmailFormatIsInvalid() {
            UserDTO userDTO = validUserDTO.toBuilder()
                    .email("invalid-email")
                    .build();

            Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Email must be valid")));
        }

        @Test
        @DisplayName("Should fail validation when email exceeds max length")
        void shouldFailValidationWhenEmailExceedsMaxLength() {
            String longEmail = "a".repeat(250) + "@example.com";
            UserDTO userDTO = validUserDTO.toBuilder()
                    .email(longEmail)
                    .build();

            Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Email cannot exceed 255 characters")));
        }

        @Test
        @DisplayName("Should fail validation when phone format is invalid")
        void shouldFailValidationWhenPhoneFormatIsInvalid() {
            UserDTO userDTO = validUserDTO.toBuilder()
                    .phone("invalid-phone")
                    .build();

            Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Phone must be valid")));
        }

        @Test
        @DisplayName("Should pass validation when phone is null")
        void shouldPassValidationWhenPhoneIsNull() {
            UserDTO userDTO = validUserDTO.toBuilder()
                    .phone(null)
                    .build();

            Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should pass validation with valid international phone")
        void shouldPassValidationWithValidInternationalPhone() {
            UserDTO userDTO = validUserDTO.toBuilder()
                    .phone("+44-123-456-7890")
                    .build();

            Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
            assertTrue(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("Helper Methods Tests")
    class HelperMethodsTests {

        @Test
        @DisplayName("Should return correct initials for both names")
        void shouldReturnCorrectInitialsForBothNames() {
            UserDTO userDTO = UserDTO.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            assertEquals("JD", userDTO.getInitials());
        }

        @Test
        @DisplayName("Should return single initial when only firstName is present")
        void shouldReturnSingleInitialWhenOnlyFirstNameIsPresent() {
            UserDTO userDTO = UserDTO.builder()
                    .firstName("John")
                    .lastName(null)
                    .build();

            assertEquals("J", userDTO.getInitials());
        }

        @Test
        @DisplayName("Should return single initial when only lastName is present")
        void shouldReturnSingleInitialWhenOnlyLastNameIsPresent() {
            UserDTO userDTO = UserDTO.builder()
                    .firstName(null)
                    .lastName("Doe")
                    .build();

            assertEquals("D", userDTO.getInitials());
        }

        @Test
        @DisplayName("Should return empty string when both names are null")
        void shouldReturnEmptyStringWhenBothNamesAreNull() {
            UserDTO userDTO = UserDTO.builder()
                    .firstName(null)
                    .lastName(null)
                    .build();

            assertEquals("", userDTO.getInitials());
        }

        @Test
        @DisplayName("Should return empty string when both names are empty")
        void shouldReturnEmptyStringWhenBothNamesAreEmpty() {
            UserDTO userDTO = UserDTO.builder()
                    .firstName("")
                    .lastName("")
                    .build();

            assertEquals("", userDTO.getInitials());
        }

        @Test
        @DisplayName("Should return uppercase initials")
        void shouldReturnUppercaseInitials() {
            UserDTO userDTO = UserDTO.builder()
                    .firstName("john")
                    .lastName("doe")
                    .build();

            assertEquals("JD", userDTO.getInitials());
        }

        @Test
        @DisplayName("Should return true when user has email contact info")
        void shouldReturnTrueWhenUserHasEmailContactInfo() {
            UserDTO userDTO = UserDTO.builder()
                    .email("john.doe@example.com")
                    .phone(null)
                    .build();

            assertTrue(userDTO.hasContactInfo());
        }

        @Test
        @DisplayName("Should return true when user has phone contact info")
        void shouldReturnTrueWhenUserHasPhoneContactInfo() {
            UserDTO userDTO = UserDTO.builder()
                    .email(null)
                    .phone("+1-555-0123")
                    .build();

            assertTrue(userDTO.hasContactInfo());
        }

        @Test
        @DisplayName("Should return true when user has both email and phone")
        void shouldReturnTrueWhenUserHasBothEmailAndPhone() {
            UserDTO userDTO = UserDTO.builder()
                    .email("john.doe@example.com")
                    .phone("+1-555-0123")
                    .build();

            assertTrue(userDTO.hasContactInfo());
        }

        @Test
        @DisplayName("Should return false when user has no contact info")
        void shouldReturnFalseWhenUserHasNoContactInfo() {
            UserDTO userDTO = UserDTO.builder()
                    .email(null)
                    .phone(null)
                    .build();

            assertFalse(userDTO.hasContactInfo());
        }

        @Test
        @DisplayName("Should return false when user has empty contact info")
        void shouldReturnFalseWhenUserHasEmptyContactInfo() {
            UserDTO userDTO = UserDTO.builder()
                    .email("   ")
                    .phone("   ")
                    .build();

            assertFalse(userDTO.hasContactInfo());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when all fields are same")
        void shouldBeEqualWhenAllFieldsAreSame() {
            UserDTO userDTO1 = UserDTO.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .phone("+1-555-0123")
                    .isActive(true)
                    .build();

            UserDTO userDTO2 = UserDTO.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .phone("+1-555-0123")
                    .isActive(true)
                    .build();

            assertEquals(userDTO1, userDTO2);
            assertEquals(userDTO1.hashCode(), userDTO2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when IDs are different")
        void shouldNotBeEqualWhenIdsAreDifferent() {
            UserDTO userDTO1 = validUserDTO.toBuilder().id(1L).build();
            UserDTO userDTO2 = validUserDTO.toBuilder().id(2L).build();

            assertNotEquals(userDTO1, userDTO2);
        }

        @Test
        @DisplayName("Should not be equal when emails are different")
        void shouldNotBeEqualWhenEmailsAreDifferent() {
            UserDTO userDTO1 = validUserDTO.toBuilder().email("john1@example.com").build();
            UserDTO userDTO2 = validUserDTO.toBuilder().email("john2@example.com").build();

            assertNotEquals(userDTO1, userDTO2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            assertNotEquals(null, validUserDTO);
        }

        @Test
        @DisplayName("Should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            assertNotEquals("not a user", validUserDTO);
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should contain all field names in toString")
        void shouldContainAllFieldNamesInToString() {
            String toStringResult = validUserDTO.toString();

            assertTrue(toStringResult.contains("UserDTO"));
            assertTrue(toStringResult.contains("id"));
            assertTrue(toStringResult.contains("firstName"));
            assertTrue(toStringResult.contains("lastName"));
            assertTrue(toStringResult.contains("email"));
            assertTrue(toStringResult.contains("phone"));
            assertTrue(toStringResult.contains("isActive"));
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should implement Serializable")
        void shouldImplementSerializable() {
            assertInstanceOf(java.io.Serializable.class, validUserDTO);
        }
    }

    @Nested
    @DisplayName("Computed Fields Tests")
    class ComputedFieldsTests {

        @Test
        @DisplayName("Should handle fullName field")
        void shouldHandleFullNameField() {
            UserDTO userDTO = UserDTO.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .fullName("John Doe")
                    .build();

            assertEquals("John Doe", userDTO.getFullName());
        }

        @Test
        @DisplayName("Should handle displayName field")
        void shouldHandleDisplayNameField() {
            UserDTO userDTO = UserDTO.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .displayName("John D.")
                    .build();

            assertEquals("John D.", userDTO.getDisplayName());
        }

        @Test
        @DisplayName("Should handle boolean flags")
        void shouldHandleBooleanFlags() {
            UserDTO userDTO = UserDTO.builder()
                    .isActive(true)
                    .hasCompleteProfile(false)
                    .build();

            assertTrue(userDTO.getIsActive());
            assertFalse(userDTO.getHasCompleteProfile());
        }

        @Test
        @DisplayName("Should handle null boolean flags")
        void shouldHandleNullBooleanFlags() {
            UserDTO userDTO = UserDTO.builder()
                    .isActive(null)
                    .hasCompleteProfile(null)
                    .build();

            assertNull(userDTO.getIsActive());
            assertNull(userDTO.getHasCompleteProfile());
        }
    }
}
