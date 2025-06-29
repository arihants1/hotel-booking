package com.hrs.hotelbooking.gateway.fallback;

import com.hrs.hotelbooking.shared.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FallbackControllerTest {

    private FallbackController fallbackController;

    @BeforeEach
    void setUp() {
        fallbackController = new FallbackController();
    }

    @Test
    void shouldReturnHotelServiceFallbackResponse() {
        // When
        ResponseEntity<ApiResponse<Object>> response = fallbackController.hotelServiceFallback();

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("HRS Hotel service is temporarily unavailable. Please try again later.",
                     response.getBody().getMessage());
        assertEquals("HRS_HOTEL_SERVICE_UNAVAILABLE", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getTimestamp());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void shouldReturnBookingServiceFallbackResponseForGet() {
        // When
        ResponseEntity<ApiResponse<Object>> response = fallbackController.bookingServiceFallback();

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("HRS Booking service is currently experiencing issues. Your booking request could not be processed. Please contact HRS support if this persists.",
                     response.getBody().getMessage());
        assertEquals("HRS_BOOKING_SERVICE_UNAVAILABLE", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getTimestamp());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void shouldReturnUserServiceFallbackResponse() {
        // When
        ResponseEntity<ApiResponse<Object>> response = fallbackController.userServiceFallback();

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("HRS User service is temporarily unavailable. Please try again later.",
                     response.getBody().getMessage());
        assertEquals("HRS_USER_SERVICE_UNAVAILABLE", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getTimestamp());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void shouldReturnGeneralFallbackResponse() {
        // When
        ResponseEntity<ApiResponse<Object>> response = fallbackController.generalFallback();

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("HRS system is experiencing technical difficulties. Please try again later or contact support.",
                     response.getBody().getMessage());
        assertEquals("HRS_GENERAL_SERVICE_UNAVAILABLE", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getTimestamp());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void shouldHaveUniqueErrorCodesForEachService() {
        // When
        ResponseEntity<ApiResponse<Object>> hotelResponse = fallbackController.hotelServiceFallback();
        ResponseEntity<ApiResponse<Object>> bookingResponse = fallbackController.bookingServiceFallback();
        ResponseEntity<ApiResponse<Object>> userResponse = fallbackController.userServiceFallback();
        ResponseEntity<ApiResponse<Object>> generalResponse = fallbackController.generalFallback();

        // Then
        String hotelErrorCode = hotelResponse.getBody().getErrorCode();
        String bookingErrorCode = bookingResponse.getBody().getErrorCode();
        String userErrorCode = userResponse.getBody().getErrorCode();
        String generalErrorCode = generalResponse.getBody().getErrorCode();

        assertNotEquals(hotelErrorCode, bookingErrorCode);
        assertNotEquals(hotelErrorCode, userErrorCode);
        assertNotEquals(hotelErrorCode, generalErrorCode);
        assertNotEquals(bookingErrorCode, userErrorCode);
        assertNotEquals(bookingErrorCode, generalErrorCode);
        assertNotEquals(userErrorCode, generalErrorCode);
    }

    @Test
    void shouldReturnServiceUnavailableStatusForAllEndpoints() {
        // When
        ResponseEntity<ApiResponse<Object>> hotelResponse = fallbackController.hotelServiceFallback();
        ResponseEntity<ApiResponse<Object>> bookingResponse = fallbackController.bookingServiceFallback();
        ResponseEntity<ApiResponse<Object>> userResponse = fallbackController.userServiceFallback();
        ResponseEntity<ApiResponse<Object>> generalResponse = fallbackController.generalFallback();

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, hotelResponse.getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, bookingResponse.getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, userResponse.getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, generalResponse.getStatusCode());
    }

    @Test
    void shouldHaveTimestampWithinReasonableTimeframe() {
        // Given
        LocalDateTime beforeCall = LocalDateTime.now().minusSeconds(1);

        // When
        ResponseEntity<ApiResponse<Object>> response = fallbackController.hotelServiceFallback();

        // Then
        LocalDateTime afterCall = LocalDateTime.now().plusSeconds(1);
        LocalDateTime responseTimestamp = response.getBody().getTimestamp();

        assertTrue(responseTimestamp.isAfter(beforeCall) || responseTimestamp.isEqual(beforeCall));
        assertTrue(responseTimestamp.isBefore(afterCall) || responseTimestamp.isEqual(afterCall));
    }

    @Test
    void shouldHaveConsistentResponseStructure() {
        // When
        ResponseEntity<ApiResponse<Object>> response = fallbackController.generalFallback();

        // Then
        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.getMessage());
        assertNotNull(body.getErrorCode());
        assertNotNull(body.getTimestamp());
        assertFalse(body.isSuccess());
        assertNull(body.getData()); // Fallback responses shouldn't have data
    }

    @Test
    void shouldHaveHRSPrefixInAllErrorCodes() {
        // When
        ResponseEntity<ApiResponse<Object>> hotelResponse = fallbackController.hotelServiceFallback();
        ResponseEntity<ApiResponse<Object>> bookingResponse = fallbackController.bookingServiceFallback();
        ResponseEntity<ApiResponse<Object>> userResponse = fallbackController.userServiceFallback();
        ResponseEntity<ApiResponse<Object>> generalResponse = fallbackController.generalFallback();

        // Then
        assertTrue(hotelResponse.getBody().getErrorCode().startsWith("HRS_"));
        assertTrue(bookingResponse.getBody().getErrorCode().startsWith("HRS_"));
        assertTrue(userResponse.getBody().getErrorCode().startsWith("HRS_"));
        assertTrue(generalResponse.getBody().getErrorCode().startsWith("HRS_"));
    }

    @Test
    void shouldHaveHRSPrefixInAllMessages() {
        // When
        ResponseEntity<ApiResponse<Object>> hotelResponse = fallbackController.hotelServiceFallback();
        ResponseEntity<ApiResponse<Object>> bookingResponse = fallbackController.bookingServiceFallback();
        ResponseEntity<ApiResponse<Object>> userResponse = fallbackController.userServiceFallback();
        ResponseEntity<ApiResponse<Object>> generalResponse = fallbackController.generalFallback();

        // Then
        assertTrue(hotelResponse.getBody().getMessage().contains("HRS"));
        assertTrue(bookingResponse.getBody().getMessage().contains("HRS"));
        assertTrue(userResponse.getBody().getMessage().contains("HRS"));
        assertTrue(generalResponse.getBody().getMessage().contains("HRS"));
    }

    @Test
    void shouldProvideSpecificGuidanceForBookingService() {
        // When
        ResponseEntity<ApiResponse<Object>> response = fallbackController.bookingServiceFallback();

        // Then
        String message = response.getBody().getMessage();
        assertTrue(message.contains("contact HRS support"),
                   "Booking service fallback should mention contacting support");
        assertTrue(message.contains("booking request could not be processed"),
                   "Booking service fallback should be specific about booking processing");
    }
}