package com.winx.fleet.api.dto;

import com.winx.fleet.domain.model.BillingModel;
import com.winx.fleet.domain.model.VehicleType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateVehicleRequest(
        @NotNull Long providerId,
        @NotNull VehicleType type,
        String description,
        @NotNull BigDecimal pricePerUnit,
        @NotNull BillingModel billingModel,
        Integer maxDurationMinutes,
        Integer maxKilometers,
        Integer minAge,
        Integer maxPersons,
        @NotNull Double latitude,
        @NotNull Double longitude
) {
}
