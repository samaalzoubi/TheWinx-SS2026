package com.winx.booking.exception;

import org.springframework.http.HttpStatus;

/** Raised when a booking id does not exist. */
public class BookingNotFoundException extends DomainException {
    public BookingNotFoundException(Long bookingId) {
        super("Booking " + bookingId + " not found.", HttpStatus.NOT_FOUND);
    }
}
