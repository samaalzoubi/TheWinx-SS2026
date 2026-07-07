package com.winx.rating.domain.exception;

// we map this to 400, thrown when a vehicle or provider score falls outside 1-5
public class InvalidScoreException extends RuntimeException {

    public InvalidScoreException(String message) {
        super(message);
    }
}
