package com.winx.booking.api.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request to create a booking. The user is resolved from the {@code X-Auth-Token} header,
 * not from the body.
 */
public record BookingCreateRequest(
        @NotNull Long vehicleId,
        @NotNull Double startLatitude,
        @NotNull Double startLongitude
) {
}
