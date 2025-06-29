package com.hrs.hotelbooking.shared.exception;

import com.hrs.hotelbooking.shared.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Nested
    @DisplayName("ResourceNotFoundException Handling Tests")
    class ResourceNotFoundExceptionHandlingTests {

        @Test
        @DisplayName("Should handle ResourceNotFoundException and return NOT_FOUND response")
        void shouldHandleResourceNotFoundExceptionAndReturnNotFoundResponse() {
            String errorMessage = "Hotel not found with id: 123";
            ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage);

            ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleResourceNotFound(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(errorMessage, response.getBody().getMessage());
            assertEquals("HRS_RESOURCE_NOT_FOUND", response.getBody().getErrorCode());
            assertFalse(response.getBody().isSuccess());
            assertNotNull(response.getBody().getTimestamp());
        }

        @Test
        @DisplayName("Should handle ResourceNotFoundException with null message")
        void shouldHandleResourceNotFoundExceptionWithNullMessage() {
            ResourceNotFoundException exception = new ResourceNotFoundException((String) null);

            ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleResourceNotFound(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNull(response.getBody().getMessage());
            assertEquals("HRS_RESOURCE_NOT_FOUND", response.getBody().getErrorCode());
            assertFalse(response.getBody().isSuccess());
        }

        @Test
        @DisplayName("Should handle ResourceNotFoundException with empty message")
        void shouldHandleResourceNotFoundExceptionWithEmptyMessage() {
            String errorMessage = "";
            ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage);

            ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleResourceNotFound(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals(errorMessage, response.getBody().getMessage());
        }

        @Test
        @DisplayName("Should handle ResourceNotFoundException for different resource types")
        void shouldHandleResourceNotFoundExceptionForDifferentResourceTypes() {
            ResourceNotFoundException hotelException = new ResourceNotFoundException("Hotel", "id", 123L);
            ResourceNotFoundException userException = new ResourceNotFoundException("User", "email", "test@example.com");
            ResourceNotFoundException bookingException = new ResourceNotFoundException("Booking", "reference", "BOOK123");

            ResponseEntity<ApiResponse<Object>> hotelResponse = globalExceptionHandler.handleResourceNotFound(hotelException);
            ResponseEntity<ApiResponse<Object>> userResponse = globalExceptionHandler.handleResourceNotFound(userException);
            ResponseEntity<ApiResponse<Object>> bookingResponse = globalExceptionHandler.handleResourceNotFound(bookingException);

            assertEquals("Hotel not found with id: 123", hotelResponse.getBody().getMessage());
            assertEquals("User not found with email: test@example.com", userResponse.getBody().getMessage());
            assertEquals("Booking not found with reference: BOOK123", bookingResponse.getBody().getMessage());
        }
    }

    @Nested
    @DisplayName("BusinessValidationException Handling Tests")
    class BusinessValidationExceptionHandlingTests {

        @Test
        @DisplayName("Should handle BusinessValidationException and return BAD_REQUEST response")
        void shouldHandleBusinessValidationExceptionAndReturnBadRequestResponse() {
            String errorMessage = "Check-out date must be after check-in date";
            BusinessValidationException exception = new BusinessValidationException(errorMessage);

            ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleBusinessValidation(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(errorMessage, response.getBody().getMessage());
            assertEquals("HRS_BUSINESS_VALIDATION_ERROR", response.getBody().getErrorCode());
            assertFalse(response.getBody().isSuccess());
            assertNotNull(response.getBody().getTimestamp());
        }

        @Test
        @DisplayName("Should handle BusinessValidationException with null message")
        void shouldHandleBusinessValidationExceptionWithNullMessage() {
            BusinessValidationException exception = new BusinessValidationException(null);

            ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleBusinessValidation(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNull(response.getBody().getMessage());
            assertEquals("HRS_BUSINESS_VALIDATION_ERROR", response.getBody().getErrorCode());
        }

        @Test
        @DisplayName("Should handle various business validation scenarios")
        void shouldHandleVariousBusinessValidationScenarios() {
            BusinessValidationException dateException = new BusinessValidationException("Invalid date range");
            BusinessValidationException capacityException = new BusinessValidationException("Insufficient room capacity");
            BusinessValidationException paymentException = new BusinessValidationException("Invalid payment amount");

            ResponseEntity<ApiResponse<Object>> dateResponse = globalExceptionHandler.handleBusinessValidation(dateException);
            ResponseEntity<ApiResponse<Object>> capacityResponse = globalExceptionHandler.handleBusinessValidation(capacityException);
            ResponseEntity<ApiResponse<Object>> paymentResponse = globalExceptionHandler.handleBusinessValidation(paymentException);

            assertEquals("Invalid date range", dateResponse.getBody().getMessage());
            assertEquals("Insufficient room capacity", capacityResponse.getBody().getMessage());
            assertEquals("Invalid payment amount", paymentResponse.getBody().getMessage());

            // All should have the same error code and status
            assertEquals("HRS_BUSINESS_VALIDATION_ERROR", dateResponse.getBody().getErrorCode());
            assertEquals("HRS_BUSINESS_VALIDATION_ERROR", capacityResponse.getBody().getErrorCode());
            assertEquals("HRS_BUSINESS_VALIDATION_ERROR", paymentResponse.getBody().getErrorCode());
        }
    }

    @Nested
    @DisplayName("MethodArgumentNotValidException Handling Tests")
    class MethodArgumentNotValidExceptionHandlingTests {

        @Test
        @DisplayName("Should handle MethodArgumentNotValidException and return BAD_REQUEST response")
        void shouldHandleMethodArgumentNotValidExceptionAndReturnBadRequestResponse() {
            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);

            FieldError fieldError1 = new FieldError("bookingDTO", "checkInDate", "Check-in date is required");
            FieldError fieldError2 = new FieldError("bookingDTO", "numberOfGuests", "Number of guests must be positive");

            when(exception.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

            ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleValidationErrors(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Validation failed for HRS request", response.getBody().getMessage());
            assertEquals("HRS_VALIDATION_ERROR", response.getBody().getErrorCode());
            assertFalse(response.getBody().isSuccess());
            assertNotNull(response.getBody().getTimestamp());

            // Check validation errors map
            @SuppressWarnings("unchecked")
            Map<String, String> errors = (Map<String, String>) response.getBody().getErrors();
            assertNotNull(errors);
            assertEquals(2, errors.size());
            assertEquals("Check-in date is required", errors.get("checkInDate"));
            assertEquals("Number of guests must be positive", errors.get("numberOfGuests"));
        }

        @Test
        @DisplayName("Should handle MethodArgumentNotValidException with single validation error")
        void shouldHandleMethodArgumentNotValidExceptionWithSingleValidationError() {
            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);

            FieldError fieldError = new FieldError("userDTO", "email", "Email must be valid");

            when(exception.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

            ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleValidationErrors(exception);

            @SuppressWarnings("unchecked")
            Map<String, String> errors = (Map<String, String>) response.getBody().getErrors();
            assertEquals(1, errors.size());
            assertEquals("Email must be valid", errors.get("email"));
        }

        @Test
        @DisplayName("Should handle MethodArgumentNotValidException with no validation errors")
        void shouldHandleMethodArgumentNotValidExceptionWithNoValidationErrors() {
            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);

            when(exception.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getAllErrors()).thenReturn(List.of());

            ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleValidationErrors(exception);

            @SuppressWarnings("unchecked")
            Map<String, String> errors = (Map<String, String>) response.getBody().getErrors();
            assertTrue(errors.isEmpty());
        }
    }

    @Nested
    @DisplayName("Generic Exception Handling Tests")
    class GenericExceptionHandlingTests {

        @Test
        @DisplayName("Should handle generic Exception and return INTERNAL_SERVER_ERROR response")
        void shouldHandleGenericExceptionAndReturnInternalServerErrorResponse() {
            Exception exception = new Exception("Unexpected error occurred");

            ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleGenericException(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("An unexpected error occurred in the HRS system", response.getBody().getMessage());
            assertEquals("HRS_INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
            assertFalse(response.getBody().isSuccess());
            assertNotNull(response.getBody().getTimestamp());
        }

        @Test
        @DisplayName("Should handle RuntimeException as generic exception")
        void shouldHandleRuntimeExceptionAsGenericException() {
            RuntimeException exception = new RuntimeException("Runtime error");

            ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleGenericException(exception);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertEquals("An unexpected error occurred in the HRS system", response.getBody().getMessage());
            assertEquals("HRS_INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
        }

        @Test
        @DisplayName("Should handle NullPointerException as generic exception")
        void shouldHandleNullPointerExceptionAsGenericException() {
            NullPointerException exception = new NullPointerException("Null pointer error");

            ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleGenericException(exception);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertEquals("An unexpected error occurred in the HRS system", response.getBody().getMessage());
            assertEquals("HRS_INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException as generic exception")
        void shouldHandleIllegalArgumentExceptionAsGenericException() {
            IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

            ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleGenericException(exception);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertEquals("An unexpected error occurred in the HRS system", response.getBody().getMessage());
            assertEquals("HRS_INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
        }
    }

    @Nested
    @DisplayName("Response Structure Tests")
    class ResponseStructureTests {

        @Test
        @DisplayName("Should return consistent response structure for all exception types")
        void shouldReturnConsistentResponseStructureForAllExceptionTypes() {
            ResourceNotFoundException resourceException = new ResourceNotFoundException("Resource not found");
            BusinessValidationException businessException = new BusinessValidationException("Business validation failed");
            Exception genericException = new Exception("Generic error");

            ResponseEntity<ApiResponse<Object>> resourceResponse = globalExceptionHandler.handleResourceNotFound(resourceException);
            ResponseEntity<ApiResponse<Object>> businessResponse = globalExceptionHandler.handleBusinessValidation(businessException);
            ResponseEntity<ApiResponse<Object>> genericResponse = globalExceptionHandler.handleGenericException(genericException);

            // All responses should have the same structure
            assertNotNull(resourceResponse.getBody().getMessage());
            assertNotNull(resourceResponse.getBody().getErrorCode());
            assertNotNull(resourceResponse.getBody().getTimestamp());
            assertFalse(resourceResponse.getBody().isSuccess());

            assertNotNull(businessResponse.getBody().getMessage());
            assertNotNull(businessResponse.getBody().getErrorCode());
            assertNotNull(businessResponse.getBody().getTimestamp());
            assertFalse(businessResponse.getBody().isSuccess());

            assertNotNull(genericResponse.getBody().getMessage());
            assertNotNull(genericResponse.getBody().getErrorCode());
            assertNotNull(genericResponse.getBody().getTimestamp());
            assertFalse(genericResponse.getBody().isSuccess());
        }

        @Test
        @DisplayName("Should return timestamp within reasonable time range")
        void shouldReturnTimestampWithinReasonableTimeRange() {
            LocalDateTime beforeCall = LocalDateTime.now().minusSeconds(1);

            Exception exception = new Exception("Test error");
            ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleGenericException(exception);

            LocalDateTime afterCall = LocalDateTime.now().plusSeconds(1);
            LocalDateTime responseTimestamp = response.getBody().getTimestamp();

            assertTrue(responseTimestamp.isAfter(beforeCall));
            assertTrue(responseTimestamp.isBefore(afterCall));
        }
    }

    @Nested
    @DisplayName("Error Code Consistency Tests")
    class ErrorCodeConsistencyTests {

        @Test
        @DisplayName("Should return consistent error codes for specific exception types")
        void shouldReturnConsistentErrorCodesForSpecificExceptionTypes() {
            ResourceNotFoundException resourceException1 = new ResourceNotFoundException("Hotel not found");
            ResourceNotFoundException resourceException2 = new ResourceNotFoundException("User not found");

            BusinessValidationException businessException1 = new BusinessValidationException("Date validation failed");
            BusinessValidationException businessException2 = new BusinessValidationException("Capacity validation failed");

            ResponseEntity<ApiResponse<Object>> resourceResponse1 = globalExceptionHandler.handleResourceNotFound(resourceException1);
            ResponseEntity<ApiResponse<Object>> resourceResponse2 = globalExceptionHandler.handleResourceNotFound(resourceException2);

            ResponseEntity<ApiResponse<Object>> businessResponse1 = globalExceptionHandler.handleBusinessValidation(businessException1);
            ResponseEntity<ApiResponse<Object>> businessResponse2 = globalExceptionHandler.handleBusinessValidation(businessException2);

            assertEquals("HRS_RESOURCE_NOT_FOUND", resourceResponse1.getBody().getErrorCode());
            assertEquals("HRS_RESOURCE_NOT_FOUND", resourceResponse2.getBody().getErrorCode());

            assertEquals("HRS_BUSINESS_VALIDATION_ERROR", businessResponse1.getBody().getErrorCode());
            assertEquals("HRS_BUSINESS_VALIDATION_ERROR", businessResponse2.getBody().getErrorCode());
        }

        @Test
        @DisplayName("Should return unique error codes for different exception types")
        void shouldReturnUniqueErrorCodesForDifferentExceptionTypes() {
            ResourceNotFoundException resourceException = new ResourceNotFoundException("Resource not found");
            BusinessValidationException businessException = new BusinessValidationException("Business validation failed");
            Exception genericException = new Exception("Generic error");

            ResponseEntity<ApiResponse<Object>> resourceResponse = globalExceptionHandler.handleResourceNotFound(resourceException);
            ResponseEntity<ApiResponse<Object>> businessResponse = globalExceptionHandler.handleBusinessValidation(businessException);
            ResponseEntity<ApiResponse<Object>> genericResponse = globalExceptionHandler.handleGenericException(genericException);

            String resourceErrorCode = resourceResponse.getBody().getErrorCode();
            String businessErrorCode = businessResponse.getBody().getErrorCode();
            String genericErrorCode = genericResponse.getBody().getErrorCode();

            assertNotEquals(resourceErrorCode, businessErrorCode);
            assertNotEquals(businessErrorCode, genericErrorCode);
            assertNotEquals(resourceErrorCode, genericErrorCode);
        }
    }
}

