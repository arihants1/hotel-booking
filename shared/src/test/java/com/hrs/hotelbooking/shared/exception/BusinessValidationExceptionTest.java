package com.hrs.hotelbooking.shared.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BusinessValidationException Tests")
class BusinessValidationExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with message only")
        void shouldCreateExceptionWithMessage() {
            String message = "Business validation failed";

            BusinessValidationException exception = new BusinessValidationException(message);

            assertEquals(message, exception.getMessage());
            assertNull(exception.getCause());
            assertTrue(exception instanceof RuntimeException);
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            String message = "Business validation failed";
            Throwable cause = new IllegalArgumentException("Invalid argument");

            BusinessValidationException exception = new BusinessValidationException(message, cause);

            assertEquals(message, exception.getMessage());
            assertEquals(cause, exception.getCause());
            assertTrue(exception instanceof RuntimeException);
        }

        @Test
        @DisplayName("Should handle null message")
        void shouldHandleNullMessage() {
            BusinessValidationException exception = new BusinessValidationException(null);

            assertNull(exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should handle empty message")
        void shouldHandleEmptyMessage() {
            String message = "";

            BusinessValidationException exception = new BusinessValidationException(message);

            assertEquals(message, exception.getMessage());
            assertNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("Business Scenario Tests")
    class BusinessScenarioTests {

        @Test
        @DisplayName("Should throw exception for invalid booking dates")
        void shouldThrowExceptionForInvalidBookingDates() {
            String message = "Check-out date must be after check-in date";

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> {
                    throw new BusinessValidationException(message);
                }
            );

            assertEquals(message, exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for insufficient room capacity")
        void shouldThrowExceptionForInsufficientRoomCapacity() {
            String message = "Number of guests exceeds room capacity";

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> {
                    throw new BusinessValidationException(message);
                }
            );

            assertEquals(message, exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid payment amount")
        void shouldThrowExceptionForInvalidPaymentAmount() {
            String message = "Payment amount cannot be negative";
            IllegalArgumentException cause = new IllegalArgumentException("Negative amount");

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> {
                    throw new BusinessValidationException(message, cause);
                }
            );

            assertEquals(message, exception.getMessage());
            assertEquals(cause, exception.getCause());
        }
    }

    @Nested
    @DisplayName("Exception Chaining Tests")
    class ExceptionChainingTests {

        @Test
        @DisplayName("Should preserve cause chain")
        void shouldPreserveCauseChain() {
            RuntimeException rootCause = new RuntimeException("Root cause");
            IllegalStateException intermediateCause = new IllegalStateException("Intermediate cause", rootCause);
            BusinessValidationException exception = new BusinessValidationException("Business validation failed", intermediateCause);

            assertEquals("Business validation failed", exception.getMessage());
            assertEquals(intermediateCause, exception.getCause());
            assertEquals(rootCause, exception.getCause().getCause());
        }

        @Test
        @DisplayName("Should handle null cause")
        void shouldHandleNullCause() {
            String message = "Business validation failed";

            BusinessValidationException exception = new BusinessValidationException(message, null);

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
            BusinessValidationException exception = new BusinessValidationException("Test message");

            StackTraceElement[] stackTrace = exception.getStackTrace();

            assertNotNull(stackTrace);
            assertTrue(stackTrace.length > 0);
            assertEquals("shouldHaveValidStackTrace", stackTrace[0].getMethodName());
        }

        @Test
        @DisplayName("Should preserve stack trace with cause")
        void shouldPreserveStackTraceWithCause() {
            RuntimeException cause = new RuntimeException("Cause message");
            BusinessValidationException exception = new BusinessValidationException("Test message", cause);

            StackTraceElement[] stackTrace = exception.getStackTrace();
            StackTraceElement[] causeStackTrace = cause.getStackTrace();

            assertNotNull(stackTrace);
            assertNotNull(causeStackTrace);
            assertTrue(stackTrace.length > 0);
            assertTrue(causeStackTrace.length > 0);
        }
    }
}