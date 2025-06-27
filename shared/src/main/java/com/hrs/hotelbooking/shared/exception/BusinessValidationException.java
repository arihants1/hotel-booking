package com.hrs.hotelbooking.shared.exception;

/**
 * HRS Business Validation Exception
 * Thrown when business validation rules are violated in the HRS system
 * 
 * @author arihants1
 */
public class BusinessValidationException extends RuntimeException {
    
    public BusinessValidationException(String message) {
        super(message);
    }
    
    public BusinessValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}