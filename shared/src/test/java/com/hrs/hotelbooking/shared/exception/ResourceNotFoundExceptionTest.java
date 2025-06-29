package com.hrs.hotelbooking.shared.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ResourceNotFoundException Tests")
class ResourceNotFoundExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with message only")
        void shouldCreateExceptionWithMessage() {
            String message = "Resource not found";

            ResourceNotFoundException exception = new ResourceNotFoundException(message);

            assertEquals(message, exception.getMessage());
            assertNull(exception.getCause());
            assertTrue(exception instanceof RuntimeException);
        }

        @Test
        @DisplayName("Should create exception with resource details")
        void shouldCreateExceptionWithResourceDetails() {
            String resourceName = "Hotel";
            String fieldName = "id";
            Object fieldValue = 123L;

            ResourceNotFoundException exception = new ResourceNotFoundException(resourceName, fieldName, fieldValue);

            String expectedMessage = String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue);
            assertEquals(expectedMessage, exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            String message = "Resource not found";
            Throwable cause = new IllegalArgumentException("Invalid ID");

            ResourceNotFoundException exception = new ResourceNotFoundException(message, cause);

            assertEquals(message, exception.getMessage());
            assertEquals(cause, exception.getCause());
            assertTrue(exception instanceof RuntimeException);
        }

        @Test
        @DisplayName("Should handle null values in resource details constructor")
        void shouldHandleNullValuesInResourceDetailsConstructor() {
            ResourceNotFoundException exception = new ResourceNotFoundException(null, null, null);

            String expectedMessage = "null not found with null: null";
            assertEquals(expectedMessage, exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should handle empty strings in resource details constructor")
        void shouldHandleEmptyStringsInResourceDetailsConstructor() {
            String resourceName = "";
            String fieldName = "";
            String fieldValue = "";

            ResourceNotFoundException exception = new ResourceNotFoundException(resourceName, fieldName, fieldValue);

            String expectedMessage = String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue);
            assertEquals(expectedMessage, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Business Scenario Tests")
    class BusinessScenarioTests {

        @Test
        @DisplayName("Should throw exception for hotel not found")
        void shouldThrowExceptionForHotelNotFound() {
            Long hotelId = 999L;

            ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> {
                    throw new ResourceNotFoundException("Hotel", "id", hotelId);
                }
            );

            assertEquals("Hotel not found with id: 999", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for user not found")
        void shouldThrowExceptionForUserNotFound() {
            String email = "nonexistent@example.com";

            ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> {
                    throw new ResourceNotFoundException("User", "email", email);
                }
            );

            assertEquals("User not found with email: nonexistent@example.com", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for booking not found")
        void shouldThrowExceptionForBookingNotFound() {
            String bookingReference = "BOOK123456";

            ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> {
                    throw new ResourceNotFoundException("Booking", "reference", bookingReference);
                }
            );

            assertEquals("Booking not found with reference: BOOK123456", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception with custom message")
        void shouldThrowExceptionWithCustomMessage() {
            String customMessage = "The requested hotel is not available in the system";

            ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> {
                    throw new ResourceNotFoundException(customMessage);
                }
            );

            assertEquals(customMessage, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Data Type Handling Tests")
    class DataTypeHandlingTests {

        @Test
        @DisplayName("Should handle Long field values")
        void shouldHandleLongFieldValues() {
            Long id = 12345L;

            ResourceNotFoundException exception = new ResourceNotFoundException("Entity", "id", id);

            assertEquals("Entity not found with id: 12345", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle String field values")
        void shouldHandleStringFieldValues() {
            String name = "Test Hotel";

            ResourceNotFoundException exception = new ResourceNotFoundException("Hotel", "name", name);

            assertEquals("Hotel not found with name: Test Hotel", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle Integer field values")
        void shouldHandleIntegerFieldValues() {
            Integer roomNumber = 101;

            ResourceNotFoundException exception = new ResourceNotFoundException("Room", "number", roomNumber);

            assertEquals("Room not found with number: 101", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle UUID field values")
        void shouldHandleUUIDFieldValues() {
            String uuid = "123e4567-e89b-12d3-a456-426614174000";

            ResourceNotFoundException exception = new ResourceNotFoundException("Booking", "uuid", uuid);

            assertEquals("Booking not found with uuid: 123e4567-e89b-12d3-a456-426614174000", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle complex object field values")
        void shouldHandleComplexObjectFieldValues() {
            Object complexObject = new Object() {
                @Override
                public String toString() {
                    return "ComplexObject{field=value}";
                }
            };

            ResourceNotFoundException exception = new ResourceNotFoundException("Entity", "complexField", complexObject);

            assertEquals("Entity not found with complexField: ComplexObject{field=value}", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Exception Chaining Tests")
    class ExceptionChainingTests {

        @Test
        @DisplayName("Should preserve cause chain")
        void shouldPreserveCauseChain() {
            RuntimeException rootCause = new RuntimeException("Database connection failed");
            IllegalStateException intermediateCause = new IllegalStateException("Repository error", rootCause);
            ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found", intermediateCause);

            assertEquals("Resource not found", exception.getMessage());
            assertEquals(intermediateCause, exception.getCause());
            assertEquals(rootCause, exception.getCause().getCause());
        }

        @Test
        @DisplayName("Should handle null cause")
        void shouldHandleNullCause() {
            String message = "Resource not found";

            ResourceNotFoundException exception = new ResourceNotFoundException(message, null);

            assertEquals(message, exception.getMessage());
            assertNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("Stack Trace Tests")
    class StackTraceTests {

        @Test
        @DisplayName("Should have valid stack trace")
        void shouldHaveValidStackTrace() {
            ResourceNotFoundException exception = new ResourceNotFoundException("Test message");

            StackTraceElement[] stackTrace = exception.getStackTrace();

            assertNotNull(stackTrace);
            assertTrue(stackTrace.length > 0);
            assertEquals("shouldHaveValidStackTrace", stackTrace[0].getMethodName());
        }

        @Test
        @DisplayName("Should preserve stack trace with cause")
        void shouldPreserveStackTraceWithCause() {
            RuntimeException cause = new RuntimeException("Database error");
            ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found", cause);

            StackTraceElement[] stackTrace = exception.getStackTrace();
            StackTraceElement[] causeStackTrace = cause.getStackTrace();

            assertNotNull(stackTrace);
            assertNotNull(causeStackTrace);
            assertTrue(stackTrace.length > 0);
            assertTrue(causeStackTrace.length > 0);
        }
    }

    @Nested
    @DisplayName("Message Formatting Tests")
    class MessageFormattingTests {

        @Test
        @DisplayName("Should format message correctly with special characters")
        void shouldFormatMessageCorrectlyWithSpecialCharacters() {
            String resourceName = "Hotel & Resort";
            String fieldName = "name";
            String fieldValue = "Grand Hotel & Spa";

            ResourceNotFoundException exception = new ResourceNotFoundException(resourceName, fieldName, fieldValue);

            assertEquals("Hotel & Resort not found with name: Grand Hotel & Spa", exception.getMessage());
        }

        @Test
        @DisplayName("Should format message correctly with numbers")
        void shouldFormatMessageCorrectlyWithNumbers() {
            String resourceName = "Room";
            String fieldName = "floor";
            Integer fieldValue = 0;

            ResourceNotFoundException exception = new ResourceNotFoundException(resourceName, fieldName, fieldValue);

            assertEquals("Room not found with floor: 0", exception.getMessage());
        }

        @Test
        @DisplayName("Should format message correctly with boolean values")
        void shouldFormatMessageCorrectlyWithBooleanValues() {
            String resourceName = "Hotel";
            String fieldName = "active";
            Boolean fieldValue = true;

            ResourceNotFoundException exception = new ResourceNotFoundException(resourceName, fieldName, fieldValue);

            assertEquals("Hotel not found with active: true", exception.getMessage());
        }
    }
}
