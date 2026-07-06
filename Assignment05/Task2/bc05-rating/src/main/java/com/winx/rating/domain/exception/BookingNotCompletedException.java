package com.winx.rating.domain.exception;

/** Thrown when a rating is attempted for a booking that is not yet COMPLETED. Maps to HTTP 409. */
public class BookingNotCompletedException extends RuntimeException {

    public BookingNotCompletedException(Long bookingId, String actualStatus) {
        super("Booking " + bookingId + " is not completed (status=" + actualStatus + ")");
    }
}
