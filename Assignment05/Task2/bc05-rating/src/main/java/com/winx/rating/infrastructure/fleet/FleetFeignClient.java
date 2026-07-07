package com.winx.rating.infrastructure.fleet;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// same booking.service.url reasoning as BookingFeignClient here, empty by default, only there for pinning during isolated testing
@FeignClient(name = "bc02-fleet-management", url = "${fleet.service.url:}")
public interface FleetFeignClient {

    @GetMapping("/api/vehicles/{id}")
    FleetVehicleResponse getVehicle(@PathVariable("id") Long id);
}
