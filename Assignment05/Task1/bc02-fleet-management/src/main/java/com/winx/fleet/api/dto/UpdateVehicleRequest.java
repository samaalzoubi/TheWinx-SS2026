package com.winx.fleet.api.dto;

import com.winx.fleet.domain.model.BillingModel;

import java.math.BigDecimal;

// we treat null fields as "leave unchanged" and left providerId/type out since they're immutable
public record UpdateVehicleRequest(
        String description,
        BigDecimal pricePerUnit,
        BillingModel billingModel,
        Integer maxDurationMinutes,
        Integer maxKilometers,
        Integer minAge,
        Integer maxPersons
) {
}
