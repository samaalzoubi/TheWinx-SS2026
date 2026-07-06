package com.winx.rating.infrastructure.fleet;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Declarative HTTP client towards bc02-fleet-management's Open Host Service,
 * resolved via Eureka service discovery (load-balanced by name, no hardcoded
 * host/port). Only the fields Rating actually needs are declared here;
 * Jackson silently ignores the rest of Fleet's vehicle JSON payload.
 *
 * <p>The optional {@code fleet.service.url} property is left empty by
 * default, which keeps normal discovery-based resolution; it exists purely
 * so an operator/tester can pin this one client to a fixed URL (bypassing
 * Eureka) when verifying this integration in isolation from other
 * discovered services.
 */
@FeignClient(name = "bc02-fleet-management", url = "${fleet.service.url:}")
public interface FleetFeignClient {

    @GetMapping("/api/vehicles/{id}")
    FleetVehicleResponse getVehicle(@PathVariable("id") Long id);
}
