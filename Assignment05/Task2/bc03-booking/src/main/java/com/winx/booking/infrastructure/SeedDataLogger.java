package com.winx.booking.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Logs the mock users/vehicles seeded in {@link MockUserClient} and
 * {@link MockVehicleClient} on startup, so a human tester knows what ids to
 * type into the UI or curl commands. No bookings are pre-created - a clean
 * start makes the demo flow clearer.
 */
@Component
public class SeedDataLogger implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataLogger.class);

    private final MockUserClient mockUserClient;
    private final MockVehicleClient mockVehicleClient;

    public SeedDataLogger(MockUserClient mockUserClient, MockVehicleClient mockVehicleClient) {
        this.mockUserClient = mockUserClient;
        this.mockVehicleClient = mockVehicleClient;
    }

    @Override
    public void run(String... args) {
        log.info("=== BC-03 Booking: seeded mock data (standalone mode) ===");
        for (long id = 1; id <= 3; id++) {
            mockUserClient.getUser(id).ifPresent(u ->
                    log.info("  User {}: {} (born {})", u.id(), u.name(), u.dateOfBirth()));
        }
        for (long id = 1; id <= 4; id++) {
            mockVehicleClient.getVehicle(id).ifPresent(v ->
                    log.info("  Vehicle {}: {} ({}), {} {} per unit, status {}, minAge {}, at ({}, {})",
                            v.id(), v.description(), v.type(), v.pricePerUnit(), v.billingModel(),
                            v.status(), v.minAge(), v.latitude(), v.longitude()));
        }
        log.info("=== Try: POST /api/bookings with body {\"userId\":1,\"vehicleId\":1} ===");
    }
}
