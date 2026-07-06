package com.winx.rating.infrastructure.fleet;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Resilience4j-guarded gateway towards bc02-fleet-management. Deliberately a
 * separate Spring bean (rather than a private method on {@code
 * ResilientBookingClient}) so that the {@link CircuitBreaker} annotation is
 * applied through the Spring AOP proxy: calls arriving here always come from
 * a different bean, so the proxy - and therefore the circuit breaker - is
 * never bypassed by self-invocation.
 *
 * <p>This gateway never throws to its caller: on failure of any kind (Fleet
 * unreachable, circuit open, timeout, even a genuine 404 for an unknown
 * vehicle) it returns the {@link #PROVIDER_UNKNOWN} sentinel instead, so
 * {@code ResilientBookingClient} never needs a try/catch around this call.
 */
@Component
public class FleetLookupGateway {

    private static final Logger log = LoggerFactory.getLogger(FleetLookupGateway.class);

    /** Sentinel returned when Fleet cannot be reached / circuit is open. */
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
