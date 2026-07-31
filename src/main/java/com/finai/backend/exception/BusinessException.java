package com.finai.backend.exception;

/**
 * Exception thrown when business logic validation fails
 */
public class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }
}
