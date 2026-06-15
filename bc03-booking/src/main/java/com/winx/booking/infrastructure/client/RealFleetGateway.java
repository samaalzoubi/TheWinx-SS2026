package com.winx.booking.infrastructure.client;

import com.winx.booking.api.dto.StatusUpdate;
import com.winx.booking.api.dto.VehicleDto;
import com.winx.booking.exception.DependencyUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Real gateway to Fleet Management over Feign, guarded by a circuit breaker.
 * Active in every profile except {@code mock}.
 *
 * <p>Fallback policy: reads that must succeed for a booking ({@code findVehicle}, status flips)
 * fail fast with {@link DependencyUnavailableException}; {@code search} degrades to an empty list
 * so the search UI stays usable when Fleet is down.
 */
@Component
@Profile("!mock")
@RequiredArgsConstructor
public class RealFleetGateway implements FleetGateway {

    private final FleetClient client;

    @Override
    @CircuitBreaker(name = "fleet", fallbackMethod = "vehicleUnavailable")
    public VehicleDto findVehicle(Long id) {
        return client.findById(id);
    }

    @Override
    @CircuitBreaker(name = "fleet", fallbackMethod = "searchUnavailable")
    public List<VehicleDto> search(double lat, double lon, double radiusKm, String type, BigDecimal maxPrice) {
        return client.search(lat, lon, radiusKm, type, maxPrice);
    }

    @Override
    @CircuitBreaker(name = "fleet", fallbackMethod = "statusUpdateFailed")
    public VehicleDto markBooked(Long vehicleId) {
        return client.updateStatus(vehicleId, new StatusUpdate("BOOKED"));
    }

    @Override
    @CircuitBreaker(name = "fleet", fallbackMethod = "statusUpdateFailed")
    public VehicleDto markAvailable(Long vehicleId) {
        return client.updateStatus(vehicleId, new StatusUpdate("AVAILABLE"));
    }

    /* ---------- fallbacks ---------- */

    @SuppressWarnings("unused")
    private VehicleDto vehicleUnavailable(Long id, Throwable t) {
        throw new DependencyUnavailableException("Fleet Management unavailable", t);
    }

    @SuppressWarnings("unused")
    private List<VehicleDto> searchUnavailable(double lat, double lon, double radiusKm,
                                               String type, BigDecimal maxPrice, Throwable t) {
        return List.of();
    }

    @SuppressWarnings("unused")
    private VehicleDto statusUpdateFailed(Long vehicleId, Throwable t) {
        throw new DependencyUnavailableException("Fleet status update failed", t);
    }
}
