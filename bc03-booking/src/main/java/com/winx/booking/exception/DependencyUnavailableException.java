package com.winx.booking.exception;

import org.springframework.http.HttpStatus;

/**
 * Raised by a circuit-breaker fallback when a downstream service (Identity, Fleet, ...)
 * is unavailable and the operation cannot proceed safely.
 */
public class DependencyUnavailableException extends DomainException {
    public DependencyUnavailableException(String message, Throwable cause) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, cause);
    }
}
