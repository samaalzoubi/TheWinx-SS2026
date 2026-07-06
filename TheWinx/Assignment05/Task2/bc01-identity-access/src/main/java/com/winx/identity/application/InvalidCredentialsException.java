package com.winx.identity.application;

/**
 * Thrown when a login attempt fails because the email/password combination
 * does not match a known, active account.
 * Mapped to HTTP 401 Unauthorized by the GlobalExceptionHandler.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
