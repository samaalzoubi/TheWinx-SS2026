package com.winx.rating.domain.exception;

/** Thrown when the referenced booking does not exist per the BookingClient. Maps to HTTP 404. */
public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(Long bookingId) {
        super("Booking not found: " + bookingId);
    }
}
