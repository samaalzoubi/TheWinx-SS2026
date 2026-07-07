package com.winx.rating.infrastructure.fleet;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// we made this its own bean for the same AOP self-invocation reason as BookingLookupGateway, and it never throws, always returns PROVIDER_UNKNOWN on failure instead
@Component
public class FleetLookupGateway {

    private static final Logger log = LoggerFactory.getLogger(FleetLookupGateway.class);

    public static final long PROVIDER_UNKNOWN = -1L;

    private final FleetFeignClient fleetFeignClient;

    public FleetLookupGateway(FleetFeignClient fleetFeignClient) {
        this.fleetFeignClient = fleetFeignClient;
    }

    @CircuitBreaker(name = "fleetClient", fallbackMethod = "resolveProviderIdFallback")
    public Long resolveProviderId(Long vehicleId) {
        Long providerId = fleetFeignClient.getVehicle(vehicleId).providerId();
        log.info("Resolved providerId={} for vehicleId={} via bc02-fleet-management (real Fleet lookup, not a fallback)",
                providerId, vehicleId);
        return providerId;
    }

    @SuppressWarnings("unused")
    private Long resolveProviderIdFallback(Long vehicleId, Throwable t) {
        log.warn("Fleet service unavailable while resolving providerId for vehicle {} ({}: {}); " +
                        "returning sentinel providerId={}",
                vehicleId, t.getClass().getSimpleName(), t.getMessage(), PROVIDER_UNKNOWN);
        return PROVIDER_UNKNOWN;
    }
}
