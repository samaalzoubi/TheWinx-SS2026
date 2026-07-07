package com.winx.rating.infrastructure.booking;

// no providerId here since Booking doesn't expose one, that's why FleetLookupGateway exists to resolve it from vehicleId instead
public record BookingFeignResponse(Long id, Long userId, Long vehicleId, String status) {
}
