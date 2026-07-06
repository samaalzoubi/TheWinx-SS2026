package com.winx.booking.domain.exception;

public class ActiveBookingExistsException extends RuntimeException {

    public ActiveBookingExistsException(Long userId) {
        super("User already has an active booking: " + userId);
    }
}
