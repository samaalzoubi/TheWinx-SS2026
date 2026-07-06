package com.winx.rating.domain.exception;

/** Thrown when a requested Rating does not exist. Maps to HTTP 404. */
public class RatingNotFoundException extends RuntimeException {

    public RatingNotFoundException(Long ratingId) {
        super("Rating not found: " + ratingId);
    }
}
