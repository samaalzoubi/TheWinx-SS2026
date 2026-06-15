package com.winx.booking.infrastructure.client;

import com.winx.booking.api.dto.VehicleDto;

import java.math.BigDecimal;
import java.util.List;

/** Booking's anti-corruption gateway to Fleet Management. */
public interface FleetGateway {

    VehicleDto findVehicle(Long id);

    List<VehicleDto> search(double lat, double lon, double radiusKm, String type, BigDecimal maxPrice);

    VehicleDto markBooked(Long vehicleId);

    VehicleDto markAvailable(Long vehicleId);
}
