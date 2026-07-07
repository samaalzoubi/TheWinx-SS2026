package com.winx.rating.domain.exception;

// we map this to 409, thrown when a rating is attempted for a booking that isn't COMPLETED yet
public class BookingNotCompletedException extends RuntimeException {

    public BookingNotCompletedException(Long bookingId, String actualStatus) {
        super("Booking " + bookingId + " is not completed (status=" + actualStatus + ")");
    }
}
