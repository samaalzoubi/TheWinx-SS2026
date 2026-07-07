package com.winx.fleet.domain.service;

import com.winx.fleet.domain.event.VehicleStatusUpdated;
import com.winx.fleet.domain.exception.VehicleNotFoundException;
import com.winx.fleet.domain.model.Vehicle;
import com.winx.fleet.domain.model.VehicleStatus;
import com.winx.fleet.domain.model.VehicleType;
import com.winx.fleet.domain.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VehicleAvailabilityService {

    private static final Logger log = LoggerFactory.getLogger(VehicleAvailabilityService.class);

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final VehicleRepository vehicleRepository;

    public VehicleAvailabilityService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional(readOnly = true)
    public List<Vehicle> findAvailableNear(double latitude, double longitude, double radiusKm,
                                            Optional<VehicleType> type, Optional<BigDecimal> maxPrice,
                                            Optional<Integer> minAge, Optional<Integer> maxDurationMinutes) {

        return vehicleRepository.findByStatus(VehicleStatus.AVAILABLE).stream()
                .filter(v -> type.isEmpty() || type.get() == v.getType())
                .filter(v -> maxPrice.isEmpty() || v.getPricingPolicy().getPricePerUnit().compareTo(maxPrice.get()) <= 0)
                .filter(v -> minAge.isEmpty() || matchesMinAge(v, minAge.get()))
                .filter(v -> maxDurationMinutes.isEmpty() || matchesMaxDuration(v, maxDurationMinutes.get()))
                .map(v -> new VehicleWithDistance(v, distanceKm(latitude, longitude,
                        v.getLocation().getLatitude(), v.getLocation().getLongitude())))
                .filter(vd -> vd.distanceKm <= radiusKm)
                .sorted(Comparator.comparingDouble(vd -> vd.distanceKm))
                .map(vd -> vd.vehicle)
                .collect(Collectors.toList());
    }

    @Transactional
    public Vehicle updateStatus(Long vehicleId, VehicleStatus newStatus) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));

        vehicle.setStatus(newStatus);
        Vehicle saved = vehicleRepository.save(vehicle);

        log.info("Domain event raised: {}", new VehicleStatusUpdated(vehicleId, newStatus));
        return saved;
    }

    private boolean matchesMinAge(Vehicle vehicle, Integer requestedMinAge) {
        Integer vehicleMinAge = vehicle.getUsageRestrictions().getMinAge();
        return vehicleMinAge == null || vehicleMinAge <= requestedMinAge;
    }

    private boolean matchesMaxDuration(Vehicle vehicle, Integer requestedMaxDuration) {
        Integer vehicleMaxDuration = vehicle.getUsageRestrictions().getMaxDurationMinutes();
        return vehicleMaxDuration == null || vehicleMaxDuration >= requestedMaxDuration;
    }

    // we use the Haversine formula here for straight-line distance between two points, in km
    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private static final class VehicleWithDistance {
        private final Vehicle vehicle;
        private final double distanceKm;

        private VehicleWithDistance(Vehicle vehicle, double distanceKm) {
            this.vehicle = vehicle;
            this.distanceKm = distanceKm;
        }
    }
}
