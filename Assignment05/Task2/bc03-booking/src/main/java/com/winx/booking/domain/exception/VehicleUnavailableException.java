package com.winx.booking.domain.exception;

public class VehicleUnavailableException extends RuntimeException {

    public VehicleUnavailableException(Long vehicleId) {
        super("Vehicle is not available: " + vehicleId);
    }
}
