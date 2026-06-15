package com.winx.booking.exception;

import org.springframework.http.HttpStatus;

/** Raised when an operation is attempted from an illegal booking state. */
public class InvalidBookingStateException extends DomainException {
    public InvalidBookingStateException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
