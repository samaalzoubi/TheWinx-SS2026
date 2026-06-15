package com.winx.booking.infrastructure.client.mock;

import com.winx.booking.api.dto.VehicleDto;
import com.winx.booking.infrastructure.client.FleetGateway;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mock Fleet gateway for solo development/demo ({@code --spring.profiles.active=mock}).
 * Returns hard-coded sample vehicles; status flips just echo the new status back.
 */
@Component
@Profile("mock")
public class MockFleetGateway implements FleetGateway {

    private VehicleDto sample(Long id, String status) {
        return new VehicleDto(
                id, 100L, "Sample e-scooter " + id, "E_SCOOTER", status,
                52.5200, 13.4050, new BigDecimal("0.20"), "PER_HOUR",
                null, null, 18, 1);
    }

    @Override
    public VehicleDto findVehicle(Long id) {
        return sample(id, "AVAILABLE");
    }

    @Override
    public List<VehicleDto> search(double lat, double lon, double radiusKm, String type, BigDecimal maxPrice) {
        return List.of(sample(1L, "AVAILABLE"), sample(2L, "AVAILABLE"));
    }

    @Override
    public VehicleDto markBooked(Long vehicleId) {
        return sample(vehicleId, "BOOKED");
    }

    @Override
    public VehicleDto markAvailable(Long vehicleId) {
        return sample(vehicleId, "AVAILABLE");
    }
}
