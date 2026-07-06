package com.winx.booking.domain.exception;

public class RestrictionViolationException extends RuntimeException {

    public RestrictionViolationException(String message) {
        super(message);
    }
}
