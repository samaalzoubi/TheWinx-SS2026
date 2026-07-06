package com.winx.booking.infrastructure;

import java.math.BigDecimal;

/**
 * Read-only projection of vehicle data as seen from the Booking context.
 * Sourced from the Fleet Management bounded context (mocked for now).
 */
public record VehicleView(
        Long id,
        Long providerId,
        String type,
        String description,
        String status,
        double latitude,
        double longitude,
        BigDecimal pricePerUnit,
        String billingModel,
        Integer maxDurationMinutes,
        Integer maxKilometers,
        Integer minAge,
        Integer maxPersons) {
}
