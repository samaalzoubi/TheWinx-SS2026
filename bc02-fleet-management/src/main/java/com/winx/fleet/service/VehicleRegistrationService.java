package com.winx.fleet.service;

import com.winx.fleet.dto.CreateVehicleRequest;
import com.winx.fleet.dto.UpdateVehicleRequest;
import com.winx.fleet.model.Vehicle;
import com.winx.fleet.model.VehicleStatus;
import com.winx.fleet.repository.VehicleRepository;
import org.springframework.stereotype.Service;

@Service
public class VehicleRegistrationService {

    private final VehicleRepository vehicleRepository;

    public VehicleRegistrationService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    // CREATE VEHICLE
    public Vehicle createVehicle(CreateVehicleRequest request) {

        Vehicle vehicle = new Vehicle();
        vehicle.setProviderId(request.providerId);
        vehicle.setType(request.type);
        vehicle.setDescription(request.description);
        vehicle.setPricePerUnit(request.pricePerUnit);
        vehicle.setBillingModel(request.billingModel);

        vehicle.setMaxDurationMinutes(request.maxDurationMinutes);
        vehicle.setMaxKilometers(request.maxKilometers);
        vehicle.setMinAge(request.minAge);
        vehicle.setMaxPersons(request.maxPersons);

        vehicle.setCurrentLatitude(request.currentLatitude);
        vehicle.setCurrentLongitude(request.currentLongitude);

        vehicle.setStatus(VehicleStatus.AVAILABLE);

        return vehicleRepository.save(vehicle);
    }

    // UPDATE VEHICLE DETAILS
    public Vehicle updateVehicle(Long id, UpdateVehicleRequest request) {

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        vehicle.setType(request.type);
        vehicle.setDescription(request.description);
        vehicle.setPricePerUnit(request.pricePerUnit);
        vehicle.setBillingModel(request.billingModel);

        vehicle.setMaxDurationMinutes(request.maxDurationMinutes);
        vehicle.setMaxKilometers(request.maxKilometers);
        vehicle.setMinAge(request.minAge);
        vehicle.setMaxPersons(request.maxPersons);

        return vehicleRepository.save(vehicle);
    }
}
