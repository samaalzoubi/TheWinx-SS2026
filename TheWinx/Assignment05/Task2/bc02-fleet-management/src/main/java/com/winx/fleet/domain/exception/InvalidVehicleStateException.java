package com.winx.fleet.domain.exception;

public class InvalidVehicleStateException extends RuntimeException {

    public InvalidVehicleStateException(String message) {
        super(message);
    }
}
