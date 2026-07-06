package com.winx.identity.application;

/**
 * Thrown when a requested account cannot be found.
 * Mapped to HTTP 404 Not Found by the GlobalExceptionHandler.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
