package com.winx.booking.domain.exception;

public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(Long vehicleId) {
        super("Vehicle not found: " + vehicleId);
    }
}
