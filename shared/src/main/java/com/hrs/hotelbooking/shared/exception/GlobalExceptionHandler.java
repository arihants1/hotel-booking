package com.hrs.hotelbooking.shared.exception;

import com.hrs.hotelbooking.shared.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * HRS Global Exception Handler
 * Centralized exception handling across all HRS microservices
 * Provides consistent error responses and logging
 * 
 * @author arihants1
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.error("HRS Resource not found: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(
            ex.getMessage(),
            "HRS_RESOURCE_NOT_FOUND",
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessValidation(BusinessValidationException ex) {
        log.error("HRS Business validation error: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(
            ex.getMessage(),
            "HRS_BUSINESS_VALIDATION_ERROR",
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("HRS Validation errors: {}", errors);
        
        ApiResponse<Object> response = ApiResponse.error(
            "Validation failed for HRS request",
            "HRS_VALIDATION_ERROR",
            LocalDateTime.now(),
            errors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred in HRS system", ex);
        
        ApiResponse<Object> response = ApiResponse.error(
            "An unexpected error occurred in the HRS system",
            "HRS_INTERNAL_SERVER_ERROR",
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}