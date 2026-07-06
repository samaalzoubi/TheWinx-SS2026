package com.winx.fleet.api.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateLocationRequest(@NotNull Double latitude, @NotNull Double longitude) {
}
