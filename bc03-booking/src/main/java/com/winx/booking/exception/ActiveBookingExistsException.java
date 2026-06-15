package com.winx.booking.exception;

import org.springframework.http.HttpStatus;

/** Raised when a user already has an ACTIVE booking (only one is allowed at a time). */
public class ActiveBookingExistsException extends DomainException {
    public ActiveBookingExistsException(Long userId) {
        super("User " + userId + " already has an active booking.", HttpStatus.CONFLICT);
    }
}
