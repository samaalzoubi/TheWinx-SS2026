package com.winx.rating.domain.exception;

// we map this to 409, thrown when a rating already exists for the given booking
public class DuplicateRatingException extends RuntimeException {

    public DuplicateRatingException(Long bookingId) {
        super("A rating already exists for booking: " + bookingId);
    }
}
