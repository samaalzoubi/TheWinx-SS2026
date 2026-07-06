package com.winx.fleet.config;

import com.winx.fleet.domain.model.BillingModel;
import com.winx.fleet.domain.model.PricingPolicy;
import com.winx.fleet.domain.model.UsageRestrictions;
import com.winx.fleet.domain.model.Vehicle;
import com.winx.fleet.domain.model.VehicleLocation;
import com.winx.fleet.domain.model.VehicleType;
import com.winx.fleet.domain.service.VehicleRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Seeds a handful of sample vehicles for two fake providerIds (1 and 2 - the
 * same ids used by the Identity & Access context's sample providers) so the
 * fleet dashboard and search UI have data to show out of the box.
 */
@Component
public class FleetSeedDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FleetSeedDataLoader.class);

    private static final double DORTMUND_LAT = 51.5136;
    private static final double DORTMUND_LON = 7.4653;

    private final VehicleRegistrationService registrationService;
    private final Random random = new Random(42);

    public FleetSeedDataLoader(VehicleRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @Override
    public void run(String... args) {
        Vehicle v1 = create(1L, VehicleType.E_SCOOTER, "City e-scooter, well maintained",
                new BigDecimal("0.20"), BillingModel.PER_KILOMETER,
                new UsageRestrictions(120, null, 16, 1));

        Vehicle v2 = create(1L, VehicleType.BICYCLE, "Standard city bike",
                new BigDecimal("1.00"), BillingModel.PER_HOUR,
                new UsageRestrictions(null, null, null, 1));

        Vehicle v3 = create(1L, VehicleType.E_BIKE, "Pedal-assist e-bike",
                new BigDecimal("2.50"), BillingModel.PER_HOUR,
                new UsageRestrictions(240, null, 14, 1));

        Vehicle v4 = create(2L, VehicleType.E_CAR, "Compact electric car, 4 seats",
                new BigDecimal("0.45"), BillingModel.PER_KILOMETER,
                new UsageRestrictions(null, 300, 18, 4));

        Vehicle v5 = create(2L, VehicleType.E_SCOOTER, "Fast e-scooter",
                new BigDecimal("0.25"), BillingModel.PER_KILOMETER,
                new UsageRestrictions(90, null, 18, 1));

        log.info("Seeded fleet vehicles with ids: {}, {}, {}, {}, {}",
                v1.getId(), v2.getId(), v3.getId(), v4.getId(), v5.getId());
    }

    private Vehicle create(Long providerId, VehicleType type, String description,
                            BigDecimal pricePerUnit, BillingModel billingModel,
                            UsageRestrictions restrictions) {
        VehicleLocation location = new VehicleLocation(randomOffset(DORTMUND_LAT), randomOffset(DORTMUND_LON));
        PricingPolicy pricingPolicy = new PricingPolicy(pricePerUnit, billingModel);
        return registrationService.createVehicle(providerId, type, description, pricingPolicy, restrictions, location);
    }

    private double randomOffset(double base) {
        double offset = (random.nextDouble() - 0.5) * 0.02; // roughly +/-1km
        return base + offset;
    }
}
