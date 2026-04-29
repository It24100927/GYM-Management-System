package com.gym.gym_management_system.exception;

/**
 * Exception thrown for business logic violations
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
