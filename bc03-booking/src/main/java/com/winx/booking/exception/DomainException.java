package com.winx.booking.exception;

import org.springframework.http.HttpStatus;

/**
 * Base class for all Booking domain/application errors. Carries an HTTP status hint
 * that {@code GlobalExceptionHandler} uses to build the response.
 */
public abstract class DomainException extends RuntimeException {

    private final HttpStatus status;

    protected DomainException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    protected DomainException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
