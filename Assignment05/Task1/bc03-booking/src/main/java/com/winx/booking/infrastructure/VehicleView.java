package com.winx.booking.infrastructure;

import java.math.BigDecimal;

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
