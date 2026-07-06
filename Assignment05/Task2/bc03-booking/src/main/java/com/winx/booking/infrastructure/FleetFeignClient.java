package com.winx.booking.infrastructure;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign client for the Fleet Management bounded context (bc02), resolved via
 * Eureka service discovery (no hardcoded host/port).
 * <p>
 * Reuses the existing {@link VehicleView} record as the response type since
 * its field names/order already match bc02's JSON payload exactly.
 */
@FeignClient(name = "bc02-fleet-management")
public interface FleetFeignClient {

    @GetMapping("/api/vehicles/{id}")
    VehicleView getVehicle(@PathVariable("id") Long id);

    @GetMapping("/api/vehicles/search")
    List<VehicleView> searchAvailable(@RequestParam("lat") double lat,
                                       @RequestParam("lon") double lon,
                                       @RequestParam("radiusKm") double radiusKm,
                                       @RequestParam(value = "type", required = false) String type,
                                       @RequestParam(value = "maxPrice", required = false) Double maxPrice);

    @PatchMapping("/api/vehicles/{id}/status")
    VehicleView updateStatus(@PathVariable("id") Long id, @RequestBody UpdateStatusRequest request);

    record UpdateStatusRequest(String status) {
    }
}
