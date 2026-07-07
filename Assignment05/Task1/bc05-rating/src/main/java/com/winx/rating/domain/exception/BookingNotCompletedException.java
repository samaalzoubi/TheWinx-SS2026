package com.winx.rating.domain.exception;

public class BookingNotCompletedException extends RuntimeException {

    public BookingNotCompletedException(Long bookingId, String actualStatus) {
        super("Booking " + bookingId + " is not completed (status=" + actualStatus + ")");
    }
}
