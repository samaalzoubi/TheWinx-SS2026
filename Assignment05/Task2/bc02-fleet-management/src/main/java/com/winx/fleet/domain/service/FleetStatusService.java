package com.winx.fleet.domain.service;

import com.winx.fleet.domain.event.VehicleLocationUpdated;
import com.winx.fleet.domain.exception.VehicleNotFoundException;
import com.winx.fleet.domain.model.Vehicle;
import com.winx.fleet.domain.model.VehicleLocation;
import com.winx.fleet.domain.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FleetStatusService {

    private static final Logger log = LoggerFactory.getLogger(FleetStatusService.class);

    private final VehicleRepository vehicleRepository;

    public FleetStatusService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional(readOnly = true)
    public List<Vehicle> getFleetOverview(Long providerId) {
        return vehicleRepository.findByProviderId(providerId);
    }

    @Transactional
    public Vehicle updateLocation(Long vehicleId, Double latitude, Double longitude) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));

        vehicle.setLocation(new VehicleLocation(latitude, longitude));
        Vehicle saved = vehicleRepository.save(vehicle);

        log.info("Domain event raised: {}", new VehicleLocationUpdated(vehicleId, latitude, longitude));
        return saved;
    }
}
