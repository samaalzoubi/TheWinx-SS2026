package com.winx.booking.infrastructure;

import com.winx.booking.domain.service.GeoMath;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory stand-in for the Fleet Management bounded context. Seeded with a
 * handful of example vehicles around Dortmund so the Booking service can be
 * exercised fully standalone. {@link #updateStatus(Long, String)} mutates
 * the in-memory map, simulating what a real HTTP call to Fleet Management
 * would do.
 */
@Component
public class MockVehicleClient implements VehicleClient {

    private final Map<Long, VehicleView> vehicles = new LinkedHashMap<>();

    public MockVehicleClient() {
        vehicles.put(1L, new VehicleView(1L, 1L, "BIKE", "City bike", "AVAILABLE",
                51.5136, 7.4653, new BigDecimal("5.00"), "PER_HOUR",
                240, 50, null, 1));
        vehicles.put(2L, new VehicleView(2L, 1L, "SCOOTER", "Electric scooter", "AVAILABLE",
                51.5150, 7.4700, new BigDecimal("8.50"), "PER_HOUR",
                180, 40, 18, 1));
        vehicles.put(3L, new VehicleView(3L, 2L, "CAR", "Compact car", "AVAILABLE",
                51.5100, 7.4600, new BigDecimal("0.35"), "PER_KILOMETER",
                720, 300, 25, 4));
        vehicles.put(4L, new VehicleView(4L, 2L, "CAR", "Family van", "AVAILABLE",
                51.5200, 7.4750, new BigDecimal("12.00"), "PER_HOUR",
                480, 200, 21, 6));
    }

    @Override
    public Optional<VehicleView> getVehicle(Long vehicleId) {
        return Optional.ofNullable(vehicles.get(vehicleId));
    }

    @Override
    public List<VehicleView> searchAvailable(double lat, double lon, double radiusKm, String type, Double maxPrice) {
        return vehicles.values().stream()
                .filter(v -> "AVAILABLE".equals(v.status()))
                .filter(v -> type == null || type.equalsIgnoreCase(v.type()))
                .filter(v -> maxPrice == null || v.pricePerUnit().doubleValue() <= maxPrice)
                .filter(v -> GeoMath.haversineKm(lat, lon, v.latitude(), v.longitude()) <= radiusKm)
                .sorted(Comparator.comparingDouble(v -> GeoMath.haversineKm(lat, lon, v.latitude(), v.longitude())))
                .collect(Collectors.toList());
    }

    @Override
    public void updateStatus(Long vehicleId, String status) {
        VehicleView current = vehicles.get(vehicleId);
        if (current == null) {
            return;
        }
        vehicles.put(vehicleId, new VehicleView(
                current.id(), current.providerId(), current.type(), current.description(), status,
                current.latitude(), current.longitude(), current.pricePerUnit(), current.billingModel(),
                current.maxDurationMinutes(), current.maxKilometers(), current.minAge(), current.maxPersons()));
    }
}
