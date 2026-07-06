package com.winx.booking.domain.service;

import com.winx.booking.infrastructure.VehicleClient;
import com.winx.booking.infrastructure.VehicleView;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Domain service used to search for available vehicles near a location.
 * Delegates to the {@link VehicleClient} gateway (mocked for now).
 */
@Service
public class VehicleSearchService {

    private final VehicleClient vehicleClient;

    public VehicleSearchService(VehicleClient vehicleClient) {
        this.vehicleClient = vehicleClient;
    }

    public List<VehicleView> findAvailableNear(double lat, double lon, double radiusKm, String type, Double maxPrice) {
        return vehicleClient.searchAvailable(lat, lon, radiusKm, type, maxPrice);
    }
}
