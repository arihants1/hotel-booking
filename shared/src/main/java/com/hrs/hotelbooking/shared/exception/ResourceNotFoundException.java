package com.hrs.hotelbooking.shared.exception;

/**
 * HRS Resource Not Found Exception
 * Thrown when a requested resource is not found in the HRS system
 * 
 * @author arihants1
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}