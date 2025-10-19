package com.cakify.exception;

/**
 * Custom exception class for all user-related errors
 */
public class UserExceptions extends RuntimeException {

    private final String errorCode;

    public UserExceptions(String message) {
        super(message);
        this.errorCode = "USER_ERROR";
    }

    public UserExceptions(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
