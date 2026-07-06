package com.winx.fleet.api.dto;

import com.winx.fleet.domain.model.BillingModel;

import java.math.BigDecimal;

/**
 * Partial update of the mutable fields of a vehicle. Any field left null is
 * left unchanged. providerId and type are immutable and not part of this
 * contract.
 */
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
