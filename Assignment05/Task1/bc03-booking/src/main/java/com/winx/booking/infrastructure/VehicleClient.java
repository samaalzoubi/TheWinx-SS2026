package com.winx.booking.infrastructure;

import java.util.List;
import java.util.Optional;

// same reasoning as PaymentClient/UserClient here
public interface VehicleClient {

    Optional<VehicleView> getVehicle(Long vehicleId);

    List<VehicleView> searchAvailable(double lat, double lon, double radiusKm, String type, Double maxPrice);

    void updateStatus(Long vehicleId, String status);
}
