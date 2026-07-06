package com.winx.fleet.api.dto;

import com.winx.fleet.domain.model.VehicleStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull VehicleStatus status) {
}
