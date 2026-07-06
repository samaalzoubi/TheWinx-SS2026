package com.winx.rating.infrastructure.booking;

/**
 * Partial projection of bc03-booking's booking representation - only the
 * fields Rating needs. Note there is no {@code providerId} here: Booking
 * does not expose one, which is exactly why {@link
 * com.winx.rating.infrastructure.fleet.FleetLookupGateway} exists to resolve
 * it from {@code vehicleId} instead.
 */
public record BookingFeignResponse(Long id, Long userId, Long vehicleId, String status) {
}
