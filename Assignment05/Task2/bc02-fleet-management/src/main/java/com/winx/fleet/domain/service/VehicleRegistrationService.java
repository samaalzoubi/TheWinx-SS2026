package com.winx.fleet.domain.service;

import com.winx.fleet.domain.event.VehicleCreated;
import com.winx.fleet.domain.event.VehicleDeleted;
import com.winx.fleet.domain.exception.InvalidVehicleStateException;
import com.winx.fleet.domain.exception.VehicleNotFoundException;
import com.winx.fleet.domain.model.PricingPolicy;
import com.winx.fleet.domain.model.UsageRestrictions;
import com.winx.fleet.domain.model.Vehicle;
import com.winx.fleet.domain.model.VehicleLocation;
import com.winx.fleet.domain.model.VehicleStatus;
import com.winx.fleet.domain.model.VehicleType;
import com.winx.fleet.domain.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Domain service responsible for the lifecycle of a Vehicle aggregate:
 * creation, mutable-field updates and deletion, enforcing the fleet
 * invariants along the way.
 */
@Service
public class VehicleRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(VehicleRegistrationService.class);

    private final VehicleRepository vehicleRepository;

    public VehicleRegistrationService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public Vehicle createVehicle(Long providerId, VehicleType type, String description,
                                  PricingPolicy pricingPolicy, UsageRestrictions usageRestrictions,
                                  VehicleLocation location) {
        validatePricingPolicy(pricingPolicy);
        validateUsageRestrictions(usageRestrictions);

        Vehicle vehicle = new Vehicle(providerId, type, description, location, pricingPolicy, usageRestrictions);
        Vehicle saved = vehicleRepository.save(vehicle);

        log.info("Domain event raised: {}", new VehicleCreated(saved.getId(), saved.getProviderId()));
        return saved;
    }

    @Transactional
    public Vehicle updateVehicle(Long id, String description, PricingPolicy pricingPolicy,
                                  UsageRestrictions usageRestrictions) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        if (description != null) {
            vehicle.setDescription(description);
        }
        if (pricingPolicy != null) {
            validatePricingPolicy(pricingPolicy);
            vehicle.setPricingPolicy(pricingPolicy);
        }
        if (usageRestrictions != null) {
            validateUsageRestrictions(usageRestrictions);
            vehicle.setUsageRestrictions(usageRestrictions);
        }

        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        if (vehicle.getStatus() == VehicleStatus.BOOKED) {
            throw new InvalidVehicleStateException("Vehicle with id " + id + " cannot be deleted while it is BOOKED");
        }

        vehicleRepository.delete(vehicle);
        log.info("Domain event raised: {}", new VehicleDeleted(id));
    }

    private void validatePricingPolicy(PricingPolicy pricingPolicy) {
        if (pricingPolicy == null || pricingPolicy.getPricePerUnit() == null
                || pricingPolicy.getPricePerUnit().signum() <= 0) {
            throw new IllegalArgumentException("pricePerUnit must be greater than 0");
        }
        if (pricingPolicy.getBillingModel() == null) {
            throw new IllegalArgumentException("billingModel is required");
        }
    }

    private void validateUsageRestrictions(UsageRestrictions restrictions) {
        if (restrictions == null) {
            return;
        }
        requireNonNegative(restrictions.getMaxDurationMinutes(), "maxDurationMinutes");
        requireNonNegative(restrictions.getMaxKilometers(), "maxKilometers");
        requireNonNegative(restrictions.getMinAge(), "minAge");
        requireNonNegative(restrictions.getMaxPersons(), "maxPersons");
    }

    private void requireNonNegative(Integer value, String fieldName) {
        if (value != null && value < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
    }
}
