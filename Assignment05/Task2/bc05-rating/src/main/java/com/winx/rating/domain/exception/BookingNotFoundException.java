package com.winx.rating.domain.exception;

// we map this to 404, thrown when BookingClient says the referenced booking doesn't exist
public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(Long bookingId) {
        super("Booking not found: " + bookingId);
    }
}
