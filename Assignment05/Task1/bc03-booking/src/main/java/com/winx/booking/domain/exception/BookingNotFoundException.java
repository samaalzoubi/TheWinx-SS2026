package com.winx.booking.domain.exception;

public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(Long bookingId) {
        super("Booking not found: " + bookingId);
    }
}
