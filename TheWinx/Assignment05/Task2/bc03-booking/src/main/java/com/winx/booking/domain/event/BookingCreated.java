package com.winx.booking.domain.event;

public record BookingCreated(Long bookingId, Long userId, Long vehicleId) {
}
