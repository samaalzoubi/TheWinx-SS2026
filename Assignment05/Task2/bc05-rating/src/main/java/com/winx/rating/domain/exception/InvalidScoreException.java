package com.winx.rating.domain.exception;

/** Thrown when a vehicle or provider score is outside the allowed 1-5 range. Maps to HTTP 400. */
public class InvalidScoreException extends RuntimeException {

    public InvalidScoreException(String message) {
        super(message);
    }
}
