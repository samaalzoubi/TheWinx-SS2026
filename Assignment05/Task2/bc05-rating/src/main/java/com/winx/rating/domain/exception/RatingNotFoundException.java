package com.winx.rating.domain.exception;

// we map this to 404, thrown when the requested rating doesn't exist
public class RatingNotFoundException extends RuntimeException {

    public RatingNotFoundException(Long ratingId) {
        super("Rating not found: " + ratingId);
    }
}
