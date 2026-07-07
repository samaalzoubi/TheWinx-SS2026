package com.winx.booking.domain.service;

import com.winx.booking.infrastructure.VehicleClient;
import com.winx.booking.infrastructure.VehicleView;
import org.springframework.stereotype.Service;

import java.util.List;

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
