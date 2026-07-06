package com.winx.booking.infrastructure;

import java.util.List;
import java.util.Optional;

/**
 * Gateway to the Fleet Management bounded context. Implemented today by
 * {@link MockVehicleClient} with hardcoded example data so that this
 * service runs fully standalone; a later integration phase can add a real
 * HTTP-based implementation without touching any calling code.
 */
public interface VehicleClient {

    Optional<VehicleView> getVehicle(Long vehicleId);

    List<VehicleView> searchAvailable(double lat, double lon, double radiusKm, String type, Double maxPrice);

    void updateStatus(Long vehicleId, String status);
}
