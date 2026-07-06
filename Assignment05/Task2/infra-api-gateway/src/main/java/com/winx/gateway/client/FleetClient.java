package com.winx.gateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "bc02-fleet-management")
public interface FleetClient {

    @GetMapping("/api/vehicles/search")
    List<VehicleResponse> search(@RequestParam("lat") double lat,
                                  @RequestParam("lon") double lon,
                                  @RequestParam("radiusKm") double radiusKm,
                                  @RequestParam(value = "type", required = false) String type,
                                  @RequestParam(value = "maxPrice", required = false) Double maxPrice);

    @GetMapping("/api/vehicles/{id}")
    VehicleResponse getVehicle(@PathVariable("id") Long id);

    @GetMapping("/api/vehicles")
    List<VehicleResponse> listByProvider(@RequestParam("providerId") Long providerId);

    @PostMapping("/api/vehicles")
    VehicleResponse create(@RequestBody CreateVehicleRequest request);

    @PutMapping("/api/vehicles/{id}")
    VehicleResponse update(@PathVariable("id") Long id, @RequestBody UpdateVehicleRequest request);

    @DeleteMapping("/api/vehicles/{id}")
    void delete(@PathVariable("id") Long id);

    record VehicleResponse(Long id, Long providerId, String type, String description, String status,
                            double latitude, double longitude, BigDecimal pricePerUnit, String billingModel,
                            Integer maxDurationMinutes, Integer maxKilometers, Integer minAge, Integer maxPersons) {
    }

    record CreateVehicleRequest(Long providerId, String type, String description, BigDecimal pricePerUnit,
                                 String billingModel, Integer maxDurationMinutes, Integer maxKilometers,
                                 Integer minAge, Integer maxPersons, double latitude, double longitude) {
    }

    record UpdateVehicleRequest(String description, BigDecimal pricePerUnit, String billingModel,
                                 Integer maxDurationMinutes, Integer maxKilometers, Integer minAge, Integer maxPersons) {
    }
}
