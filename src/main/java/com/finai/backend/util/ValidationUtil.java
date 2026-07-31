package com.finai.backend.util;

import com.finai.backend.exception.BadRequestException;

import java.util.regex.Pattern;

/**
 * Utility class for common validation operations
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = 
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    private static final Pattern PHONE_PATTERN = 
            Pattern.compile("^[+]?[0-9]{10,15}$");

    private ValidationUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Validate that a string is not null or empty
     */
    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException(fieldName + " cannot be empty");
        }
    }

    /**
     * Validate email format
     */
    public static void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BadRequestException("Invalid email format");
        }
    }

    /**
     * Validate phone number format
     */
    public static void validatePhoneNumber(String phone) {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new BadRequestException("Invalid phone number format");
        }
    }

    /**
     * Validate that a value is positive
     */
    public static void validatePositive(Number value, String fieldName) {
        if (value == null || value.doubleValue() <= 0) {
            throw new BadRequestException(fieldName + " must be positive");
        }
    }

    /**
     * Validate that a value is not negative
     */
    public static void validateNonNegative(Number value, String fieldName) {
        if (value == null || value.doubleValue() < 0) {
            throw new BadRequestException(fieldName + " cannot be negative");
        }
    }
}
