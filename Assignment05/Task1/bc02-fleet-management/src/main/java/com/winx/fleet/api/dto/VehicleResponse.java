package com.winx.fleet.api.dto;

import com.winx.fleet.domain.model.BillingModel;
import com.winx.fleet.domain.model.Vehicle;
import com.winx.fleet.domain.model.VehicleStatus;
import com.winx.fleet.domain.model.VehicleType;

import java.math.BigDecimal;

public record VehicleResponse(
        Long id,
        Long providerId,
        VehicleType type,
        String description,
        VehicleStatus status,
        Double latitude,
        Double longitude,
        BigDecimal pricePerUnit,
        BillingModel billingModel,
        Integer maxDurationMinutes,
        Integer maxKilometers,
        Integer minAge,
        Integer maxPersons
) {

    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getProviderId(),
                vehicle.getType(),
                vehicle.getDescription(),
                vehicle.getStatus(),
                vehicle.getLocation() != null ? vehicle.getLocation().getLatitude() : null,
                vehicle.getLocation() != null ? vehicle.getLocation().getLongitude() : null,
                vehicle.getPricingPolicy() != null ? vehicle.getPricingPolicy().getPricePerUnit() : null,
                vehicle.getPricingPolicy() != null ? vehicle.getPricingPolicy().getBillingModel() : null,
                vehicle.getUsageRestrictions() != null ? vehicle.getUsageRestrictions().getMaxDurationMinutes() : null,
                vehicle.getUsageRestrictions() != null ? vehicle.getUsageRestrictions().getMaxKilometers() : null,
                vehicle.getUsageRestrictions() != null ? vehicle.getUsageRestrictions().getMinAge() : null,
                vehicle.getUsageRestrictions() != null ? vehicle.getUsageRestrictions().getMaxPersons() : null
        );
    }
}
