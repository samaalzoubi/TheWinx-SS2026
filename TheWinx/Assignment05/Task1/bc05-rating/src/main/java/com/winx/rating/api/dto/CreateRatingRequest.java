package com.winx.rating.api.dto;

import jakarta.validation.constraints.NotNull;

public record CreateRatingRequest(
        @NotNull Long bookingId,
        @NotNull Long userId,
        @NotNull Integer vehicleScore,
        @NotNull Integer providerScore,
        String comment
) {
}
