package com.winx.booking.api.dto;

import java.math.BigDecimal;

/**
 * Booking's ACL view of a Vehicle, as read from Fleet Management
 * ({@code GET /vehicles/{id}} and {@code GET /vehicles/search}).
 *
 * <p>This is the contract Booking expects from Fleet; unknown JSON fields are ignored, so Fleet
 * may expose more. Restriction fields are carried for the (deferred) restriction-validation pass.
 * Assumed flat shape — to be reconciled with Member B's Fleet DTO during integration.
 */
public record VehicleDto(
        Long vehicleId,
        Long providerId,
        String description,
        String type,          // E_SCOOTER | BICYCLE | E_BIKE | E_CAR
        String status,        // AVAILABLE | BOOKED
        Double latitude,
        Double longitude,
        BigDecimal pricePerUnit,
        String billingModel,  // PER_HOUR | PER_KILOMETER
        Integer maxDurationMins,
        Integer maxKilometers,
        Integer minAge,
        Integer maxPersons
) {
}
