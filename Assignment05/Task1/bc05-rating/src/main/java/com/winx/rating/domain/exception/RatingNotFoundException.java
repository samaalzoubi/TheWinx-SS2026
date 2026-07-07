package com.winx.rating.domain.exception;

public class RatingNotFoundException extends RuntimeException {

    public RatingNotFoundException(Long ratingId) {
        super("Rating not found: " + ratingId);
    }
}
