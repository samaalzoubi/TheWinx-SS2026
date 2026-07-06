package com.winx.fleet.api;

import com.winx.fleet.api.dto.CreateVehicleRequest;
import com.winx.fleet.api.dto.UpdateLocationRequest;
import com.winx.fleet.api.dto.UpdateStatusRequest;
import com.winx.fleet.api.dto.UpdateVehicleRequest;
import com.winx.fleet.api.dto.VehicleResponse;
import com.winx.fleet.domain.exception.VehicleNotFoundException;
import com.winx.fleet.domain.model.PricingPolicy;
import com.winx.fleet.domain.model.UsageRestrictions;
import com.winx.fleet.domain.model.Vehicle;
import com.winx.fleet.domain.model.VehicleLocation;
import com.winx.fleet.domain.model.VehicleType;
import com.winx.fleet.domain.repository.VehicleRepository;
import com.winx.fleet.domain.service.FleetStatusService;
import com.winx.fleet.domain.service.VehicleAvailabilityService;
import com.winx.fleet.domain.service.VehicleRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleRegistrationService registrationService;
    private final FleetStatusService fleetStatusService;
    private final VehicleAvailabilityService availabilityService;
    private final VehicleRepository vehicleRepository;

    public VehicleController(VehicleRegistrationService registrationService,
                              FleetStatusService fleetStatusService,
                              VehicleAvailabilityService availabilityService,
                              VehicleRepository vehicleRepository) {
        this.registrationService = registrationService;
        this.fleetStatusService = fleetStatusService;
        this.availabilityService = availabilityService;
        this.vehicleRepository = vehicleRepository;
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody CreateVehicleRequest request) {
        PricingPolicy pricingPolicy = new PricingPolicy(request.pricePerUnit(), request.billingModel());
        UsageRestrictions restrictions = new UsageRestrictions(
                request.maxDurationMinutes(), request.maxKilometers(), request.minAge(), request.maxPersons());
        VehicleLocation location = new VehicleLocation(request.latitude(), request.longitude());

        Vehicle created = registrationService.createVehicle(
                request.providerId(), request.type(), request.description(), pricingPolicy, restrictions, location);

        return ResponseEntity.status(HttpStatus.CREATED).body(VehicleResponse.from(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getById(@PathVariable Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));
        return ResponseEntity.ok(VehicleResponse.from(vehicle));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> update(@PathVariable Long id, @RequestBody UpdateVehicleRequest request) {
        PricingPolicy pricingPolicy = null;
        if (request.pricePerUnit() != null || request.billingModel() != null) {
            Vehicle current = vehicleRepository.findById(id).orElseThrow(() -> new VehicleNotFoundException(id));
            BigDecimal price = request.pricePerUnit() != null ? request.pricePerUnit()
                    : current.getPricingPolicy().getPricePerUnit();
            var billingModel = request.billingModel() != null ? request.billingModel()
                    : current.getPricingPolicy().getBillingModel();
            pricingPolicy = new PricingPolicy(price, billingModel);
        }

        UsageRestrictions restrictions = null;
        if (request.maxDurationMinutes() != null || request.maxKilometers() != null
                || request.minAge() != null || request.maxPersons() != null) {
            restrictions = new UsageRestrictions(
                    request.maxDurationMinutes(), request.maxKilometers(), request.minAge(), request.maxPersons());
        }

        Vehicle updated = registrationService.updateVehicle(id, request.description(), pricingPolicy, restrictions);
        return ResponseEntity.ok(VehicleResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        registrationService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> byProvider(@RequestParam Long providerId) {
        List<VehicleResponse> vehicles = fleetStatusService.getFleetOverview(providerId).stream()
                .map(VehicleResponse::from)
                .toList();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/search")
    public ResponseEntity<List<VehicleResponse>> search(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam double radiusKm,
            @RequestParam(required = false) VehicleType type,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxDurationMinutes) {

        List<VehicleResponse> results = availabilityService.findAvailableNear(
                        lat, lon, radiusKm,
                        Optional.ofNullable(type), Optional.ofNullable(maxPrice),
                        Optional.ofNullable(minAge), Optional.ofNullable(maxDurationMinutes))
                .stream()
                .map(VehicleResponse::from)
                .toList();

        return ResponseEntity.ok(results);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<VehicleResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        Vehicle updated = availabilityService.updateStatus(id, request.status());
        return ResponseEntity.ok(VehicleResponse.from(updated));
    }

    @PatchMapping("/{id}/location")
    public ResponseEntity<VehicleResponse> updateLocation(@PathVariable Long id, @Valid @RequestBody UpdateLocationRequest request) {
        Vehicle updated = fleetStatusService.updateLocation(id, request.latitude(), request.longitude());
        return ResponseEntity.ok(VehicleResponse.from(updated));
    }
}
