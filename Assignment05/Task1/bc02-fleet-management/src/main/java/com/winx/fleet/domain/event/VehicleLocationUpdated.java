package com.winx.fleet.domain.event;

public record VehicleLocationUpdated(Long vehicleId, Double latitude, Double longitude) {
}
