package com.hrs.hotelbooking.shared.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ApiResponse Tests")
class ApiResponseTest {

    private ObjectMapper objectMapper;
    private LocalDateTime testTimestamp;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        testTimestamp = LocalDateTime.of(2025, 6, 29, 15, 30, 45);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create ApiResponse with no-args constructor")
        void shouldCreateApiResponseWithNoArgsConstructor() {
            ApiResponse<String> response = new ApiResponse<>();

            assertNotNull(response);
            assertFalse(response.isSuccess());
            assertNull(response.getMessage());
            assertNull(response.getErrorCode());
            assertNull(response.getData());
            assertNull(response.getErrors());
            assertNull(response.getTimestamp());
        }

        @Test
        @DisplayName("Should create ApiResponse with all-args constructor")
        void shouldCreateApiResponseWithAllArgsConstructor() {
            String data = "Test data";
            Map<String, String> errors = new HashMap<>();
            errors.put("field", "error message");

            ApiResponse<String> response = new ApiResponse<>(
                true,
                "Success message",
                null,
                data,
                errors,
                testTimestamp
            );

            assertTrue(response.isSuccess());
            assertEquals("Success message", response.getMessage());
            assertNull(response.getErrorCode());
            assertEquals(data, response.getData());
            assertEquals(errors, response.getErrors());
            assertEquals(testTimestamp, response.getTimestamp());
        }

        @Test
        @DisplayName("Should create ApiResponse with builder pattern")
        void shouldCreateApiResponseWithBuilderPattern() {
            String data = "Test data";

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(true)
                    .message("Builder message")
                    .data(data)
                    .timestamp(testTimestamp)
                    .build();

            assertTrue(response.isSuccess());
            assertEquals("Builder message", response.getMessage());
            assertEquals(data, response.getData());
            assertEquals(testTimestamp, response.getTimestamp());
        }
    }

    @Nested
    @DisplayName("Success Factory Method Tests")
    class SuccessFactoryMethodTests {

        @Test
        @DisplayName("Should create success response with data only")
        void shouldCreateSuccessResponseWithDataOnly() {
            String testData = "Hotel booking successful";
            LocalDateTime beforeCall = LocalDateTime.now().minusSeconds(1);

            ApiResponse<String> response = ApiResponse.success(testData);

            LocalDateTime afterCall = LocalDateTime.now().plusSeconds(1);

            assertTrue(response.isSuccess());
            assertEquals("HRS operation completed successfully", response.getMessage());
            assertEquals(testData, response.getData());
            assertNull(response.getErrorCode());
            assertNull(response.getErrors());
            assertNotNull(response.getTimestamp());
            assertTrue(response.getTimestamp().isAfter(beforeCall));
            assertTrue(response.getTimestamp().isBefore(afterCall));
        }

        @Test
        @DisplayName("Should create success response with data and custom message")
        void shouldCreateSuccessResponseWithDataAndCustomMessage() {
            String testData = "Hotel created successfully";
            String customMessage = "Hotel has been registered in HRS system";
            LocalDateTime beforeCall = LocalDateTime.now().minusSeconds(1);

            ApiResponse<String> response = ApiResponse.success(testData, customMessage);

            LocalDateTime afterCall = LocalDateTime.now().plusSeconds(1);

            assertTrue(response.isSuccess());
            assertEquals(customMessage, response.getMessage());
            assertEquals(testData, response.getData());
            assertNull(response.getErrorCode());
            assertNull(response.getErrors());
            assertNotNull(response.getTimestamp());
            assertTrue(response.getTimestamp().isAfter(beforeCall));
            assertTrue(response.getTimestamp().isBefore(afterCall));
        }

        @Test
        @DisplayName("Should create success response with null data")
        void shouldCreateSuccessResponseWithNullData() {
            ApiResponse<String> response = ApiResponse.success(null);

            assertTrue(response.isSuccess());
            assertEquals("HRS operation completed successfully", response.getMessage());
            assertNull(response.getData());
            assertNull(response.getErrorCode());
            assertNull(response.getErrors());
            assertNotNull(response.getTimestamp());
        }

        @Test
        @DisplayName("Should create success response with complex data types")
        void shouldCreateSuccessResponseWithComplexDataTypes() {
            Map<String, Object> complexData = new HashMap<>();
            complexData.put("hotelId", 123L);
            complexData.put("name", "Grand Hotel");
            complexData.put("rating", 4.5);
            complexData.put("amenities", List.of("WiFi", "Pool", "Gym"));

            ApiResponse<Map<String, Object>> response = ApiResponse.success(complexData, "Hotel details retrieved");

            assertTrue(response.isSuccess());
            assertEquals("Hotel details retrieved", response.getMessage());
            assertEquals(complexData, response.getData());
            assertEquals(123L, response.getData().get("hotelId"));
            assertEquals("Grand Hotel", response.getData().get("name"));
        }
    }

    @Nested
    @DisplayName("Error Factory Method Tests")
    class ErrorFactoryMethodTests {

        @Test
        @DisplayName("Should create error response with message, error code, and timestamp")
        void shouldCreateErrorResponseWithMessageErrorCodeAndTimestamp() {
            String errorMessage = "Hotel not found";
            String errorCode = "HRS_HOTEL_NOT_FOUND";

            ApiResponse<Object> response = ApiResponse.error(errorMessage, errorCode, testTimestamp);

            assertFalse(response.isSuccess());
            assertEquals(errorMessage, response.getMessage());
            assertEquals(errorCode, response.getErrorCode());
            assertEquals(testTimestamp, response.getTimestamp());
            assertNull(response.getData());
            assertNull(response.getErrors());
        }

        @Test
        @DisplayName("Should create error response with validation errors")
        void shouldCreateErrorResponseWithValidationErrors() {
            String errorMessage = "Validation failed";
            String errorCode = "HRS_VALIDATION_ERROR";
            Map<String, String> validationErrors = new HashMap<>();
            validationErrors.put("email", "Email is required");
            validationErrors.put("phone", "Phone number format is invalid");

            ApiResponse<Object> response = ApiResponse.error(errorMessage, errorCode, testTimestamp, validationErrors);

            assertFalse(response.isSuccess());
            assertEquals(errorMessage, response.getMessage());
            assertEquals(errorCode, response.getErrorCode());
            assertEquals(testTimestamp, response.getTimestamp());
            assertEquals(validationErrors, response.getErrors());
            assertNull(response.getData());
        }

        @Test
        @DisplayName("Should create error response with null values")
        void shouldCreateErrorResponseWithNullValues() {
            ApiResponse<Object> response = ApiResponse.error(null, null, null);

            assertFalse(response.isSuccess());
            assertNull(response.getMessage());
            assertNull(response.getErrorCode());
            assertNull(response.getTimestamp());
            assertNull(response.getData());
            assertNull(response.getErrors());
        }

        @Test
        @DisplayName("Should create error response with empty error code")
        void shouldCreateErrorResponseWithEmptyErrorCode() {
            String errorMessage = "Something went wrong";
            String errorCode = "";

            ApiResponse<Object> response = ApiResponse.error(errorMessage, errorCode, testTimestamp);

            assertFalse(response.isSuccess());
            assertEquals(errorMessage, response.getMessage());
            assertEquals(errorCode, response.getErrorCode());
            assertEquals(testTimestamp, response.getTimestamp());
        }

        @Test
        @DisplayName("Should create error response with complex error objects")
        void shouldCreateErrorResponseWithComplexErrorObjects() {
            String errorMessage = "Business validation failed";
            String errorCode = "HRS_BUSINESS_ERROR";

            Map<String, Object> complexErrors = new HashMap<>();
            complexErrors.put("bookingRules", List.of("Check-out must be after check-in", "Maximum 30 days stay"));
            complexErrors.put("availabilityErrors", Map.of("roomType", "SUITE", "availableRooms", 0));

            ApiResponse<Object> response = ApiResponse.error(errorMessage, errorCode, testTimestamp, complexErrors);

            assertFalse(response.isSuccess());
            assertEquals(errorMessage, response.getMessage());
            assertEquals(errorCode, response.getErrorCode());
            assertEquals(complexErrors, response.getErrors());
        }
    }

    @Nested
    @DisplayName("Generic Type Handling Tests")
    class GenericTypeHandlingTests {

        @Test
        @DisplayName("Should handle String data type")
        void shouldHandleStringDataType() {
            String stringData = "Simple string response";
            ApiResponse<String> response = ApiResponse.success(stringData);

            assertEquals(stringData, response.getData());
            assertTrue(response.getData() instanceof String);
        }

        @Test
        @DisplayName("Should handle Integer data type")
        void shouldHandleIntegerDataType() {
            Integer integerData = 42;
            ApiResponse<Integer> response = ApiResponse.success(integerData);

            assertEquals(integerData, response.getData());
            assertTrue(response.getData() instanceof Integer);
        }

        @Test
        @DisplayName("Should handle List data type")
        void shouldHandleListDataType() {
            List<String> listData = List.of("Hotel A", "Hotel B", "Hotel C");
            ApiResponse<List<String>> response = ApiResponse.success(listData, "Hotels retrieved");

            assertEquals(listData, response.getData());
            assertEquals(3, response.getData().size());
            assertTrue(response.getData().contains("Hotel A"));
        }

        @Test
        @DisplayName("Should handle Map data type")
        void shouldHandleMapDataType() {
            Map<String, Object> mapData = new HashMap<>();
            mapData.put("totalBookings", 100);
            mapData.put("revenue", 50000.0);

            ApiResponse<Map<String, Object>> response = ApiResponse.success(mapData, "Statistics retrieved");

            assertEquals(mapData, response.getData());
            assertEquals(100, response.getData().get("totalBookings"));
            assertEquals(50000.0, response.getData().get("revenue"));
        }

        @Test
        @DisplayName("Should handle custom object data type")
        void shouldHandleCustomObjectDataType() {
            TestHotel hotel = new TestHotel("Grand Hotel", 5);
            ApiResponse<TestHotel> response = ApiResponse.success(hotel);

            assertEquals(hotel, response.getData());
            assertEquals("Grand Hotel", response.getData().getName());
            assertEquals(5, response.getData().getRating());
        }
    }

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        @Test
        @DisplayName("Should serialize success response to JSON correctly")
        void shouldSerializeSuccessResponseToJsonCorrectly() throws Exception {
            ApiResponse<String> response = ApiResponse.success("test data", "Success message");

            String json = objectMapper.writeValueAsString(response);

            assertTrue(json.contains("\"success\":true"));
            assertTrue(json.contains("\"message\":\"Success message\""));
            assertTrue(json.contains("\"data\":\"test data\""));
            assertFalse(json.contains("\"errorCode\""));
            assertFalse(json.contains("\"errors\""));
        }

        @Test
        @DisplayName("Should serialize error response to JSON correctly")
        void shouldSerializeErrorResponseToJsonCorrectly() throws Exception {
            Map<String, String> errors = Map.of("field", "error message");
            ApiResponse<Object> response = ApiResponse.error("Error occurred", "ERROR_CODE", testTimestamp, errors);

            String json = objectMapper.writeValueAsString(response);

            assertTrue(json.contains("\"success\":false"));
            assertTrue(json.contains("\"message\":\"Error occurred\""));
            assertTrue(json.contains("\"errorCode\":\"ERROR_CODE\""));
            assertTrue(json.contains("\"errors\""));
            assertTrue(json.contains("\"field\":\"error message\""));
            assertFalse(json.contains("\"data\""));
        }

        @Test
        @DisplayName("Should exclude null fields from JSON serialization")
        void shouldExcludeNullFieldsFromJsonSerialization() throws Exception {
            ApiResponse<String> response = ApiResponse.success("data only");

            String json = objectMapper.writeValueAsString(response);

            assertFalse(json.contains("\"errorCode\""));
            assertFalse(json.contains("\"errors\""));
            assertTrue(json.contains("\"success\""));
            assertTrue(json.contains("\"message\""));
            assertTrue(json.contains("\"data\""));
            assertTrue(json.contains("\"timestamp\""));
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when all fields are same")
        void shouldBeEqualWhenAllFieldsAreSame() {
            ApiResponse<String> response1 = ApiResponse.success("test", "message");
            ApiResponse<String> response2 = ApiResponse.success("test", "message");

            // Set same timestamp to make them equal
            LocalDateTime timestamp = LocalDateTime.now();
            response1.setTimestamp(timestamp);
            response2.setTimestamp(timestamp);

            assertEquals(response1, response2);
            assertEquals(response1.hashCode(), response2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when data is different")
        void shouldNotBeEqualWhenDataIsDifferent() {
            ApiResponse<String> response1 = ApiResponse.success("data1");
            ApiResponse<String> response2 = ApiResponse.success("data2");

            assertNotEquals(response1, response2);
        }

        @Test
        @DisplayName("Should not be equal when success flag is different")
        void shouldNotBeEqualWhenSuccessFlagIsDifferent() {
            ApiResponse<String> successResponse = ApiResponse.success("data");
            ApiResponse<Object> errorResponse = ApiResponse.error("error", "CODE", LocalDateTime.now());

            assertNotEquals(successResponse, errorResponse);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            ApiResponse<String> response = ApiResponse.success("data");

            assertNotEquals(response, null);
        }

        @Test
        @DisplayName("Should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            ApiResponse<String> response = ApiResponse.success("data");

            assertNotEquals(response, "not an ApiResponse");
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should contain all field names in toString")
        void shouldContainAllFieldNamesInToString() {
            Map<String, String> errors = Map.of("field", "error");
            ApiResponse<String> response = ApiResponse.error("Error message", "ERROR_CODE", testTimestamp, errors);

            String toStringResult = response.toString();

            assertTrue(toStringResult.contains("ApiResponse"));
            assertTrue(toStringResult.contains("success"));
            assertTrue(toStringResult.contains("message"));
            assertTrue(toStringResult.contains("errorCode"));
            assertTrue(toStringResult.contains("errors"));
            assertTrue(toStringResult.contains("timestamp"));
        }
    }

    @Nested
    @DisplayName("Timestamp Handling Tests")
    class TimestampHandlingTests {

        @Test
        @DisplayName("Should handle timestamp formatting correctly")
        void shouldHandleTimestampFormattingCorrectly() {
            LocalDateTime specificTime = LocalDateTime.of(2025, 6, 29, 15, 30, 45);
            ApiResponse<Object> response = ApiResponse.error("Error", "CODE", specificTime);

            assertEquals(specificTime, response.getTimestamp());
        }

        @Test
        @DisplayName("Should generate current timestamp for success responses")
        void shouldGenerateCurrentTimestampForSuccessResponses() {
            LocalDateTime beforeCall = LocalDateTime.now().minusSeconds(1);
            ApiResponse<String> response = ApiResponse.success("data");
            LocalDateTime afterCall = LocalDateTime.now().plusSeconds(1);

            assertNotNull(response.getTimestamp());
            assertTrue(response.getTimestamp().isAfter(beforeCall));
            assertTrue(response.getTimestamp().isBefore(afterCall));
        }
    }

    // Helper class for testing custom object serialization
    private static class TestHotel {
        private String name;
        private int rating;

        public TestHotel(String name, int rating) {
            this.name = name;
            this.rating = rating;
        }

        public String getName() { return name; }
        public int getRating() { return rating; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestHotel testHotel = (TestHotel) obj;
            return rating == testHotel.rating && name.equals(testHotel.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode() + rating;
        }
    }
}
