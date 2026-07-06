package com.winx.fleet.domain.event;

import com.winx.fleet.domain.model.VehicleStatus;

public record VehicleStatusUpdated(Long vehicleId, VehicleStatus newStatus) {
}
