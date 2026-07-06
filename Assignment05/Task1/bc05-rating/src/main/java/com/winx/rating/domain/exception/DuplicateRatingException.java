package com.winx.rating.domain.exception;

/** Thrown when a Rating already exists for a given booking. Maps to HTTP 409. */
public class DuplicateRatingException extends RuntimeException {

    public DuplicateRatingException(Long bookingId) {
        super("A rating already exists for booking: " + bookingId);
    }
}
