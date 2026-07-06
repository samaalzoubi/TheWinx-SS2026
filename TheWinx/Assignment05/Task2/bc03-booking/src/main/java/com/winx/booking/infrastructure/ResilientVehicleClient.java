package com.winx.booking.infrastructure;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Real HTTP-backed {@link VehicleClient} that calls the Fleet Management
 * service (bc02) via Feign/Eureka, guarded by a Resilience4j circuit breaker
 * named {@code fleetClient}. Falls back to {@link MockVehicleClient}'s
 * in-memory seed data whenever the remote call fails technically (timeout,
 * connection refused, 5xx).
 */
@Component
@Primary
public class ResilientVehicleClient implements VehicleClient {

    private static final Logger log = LoggerFactory.getLogger(ResilientVehicleClient.class);

    private final FleetFeignClient feignClient;
    private final MockVehicleClient fallbackClient;

    public ResilientVehicleClient(FleetFeignClient feignClient, MockVehicleClient fallbackClient) {
        this.feignClient = feignClient;
        this.fallbackClient = fallbackClient;
    }

    @Override
    @CircuitBreaker(name = "fleetClient", fallbackMethod = "getVehicleFallback")
    public Optional<VehicleView> getVehicle(Long vehicleId) {
        try {
            return Optional.of(feignClient.getVehicle(vehicleId));
        } catch (FeignException.NotFound e) {
            // A legitimate "vehicle does not exist" - not a technical failure, so
            // don't let it count against the circuit breaker's failure rate.
            return Optional.empty();
        }
    }

    // Must NOT be private: resilience4j-spring invokes fallback methods via
    // reflection on the AOP proxy itself, and a private method bypasses the
    // proxy's target delegation, leaving injected fields null (see
    // https://github.com/resilience4j/resilience4j/issues/1993).
    public Optional<VehicleView> getVehicleFallback(Long vehicleId, Throwable t) {
        log.warn("Fleet client circuit breaker fallback for vehicle {}: {}", vehicleId, t.toString());
        return fallbackClient.getVehicle(vehicleId);
    }

    @Override
    @CircuitBreaker(name = "fleetClient", fallbackMethod = "searchAvailableFallback")
    public List<VehicleView> searchAvailable(double lat, double lon, double radiusKm, String type, Double maxPrice) {
        return feignClient.searchAvailable(lat, lon, radiusKm, type, maxPrice);
    }

    public List<VehicleView> searchAvailableFallback(double lat, double lon, double radiusKm, String type,
                                                       Double maxPrice, Throwable t) {
        log.warn("Fleet client circuit breaker fallback for searchAvailable: {}", t.toString());
        return fallbackClient.searchAvailable(lat, lon, radiusKm, type, maxPrice);
    }

    @Override
    @CircuitBreaker(name = "fleetClient", fallbackMethod = "updateStatusFallback")
    public void updateStatus(Long vehicleId, String status) {
        feignClient.updateStatus(vehicleId, new FleetFeignClient.UpdateStatusRequest(status));
    }

    public void updateStatusFallback(Long vehicleId, String status, Throwable t) {
        log.warn("Fleet client circuit breaker fallback for updateStatus({}, {}): {}", vehicleId, status, t.toString());
        fallbackClient.updateStatus(vehicleId, status);
    }
}
