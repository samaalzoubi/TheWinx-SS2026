package com.winx.rating.domain.exception;

public class DuplicateRatingException extends RuntimeException {

    public DuplicateRatingException(Long bookingId) {
        super("A rating already exists for booking: " + bookingId);
    }
}
