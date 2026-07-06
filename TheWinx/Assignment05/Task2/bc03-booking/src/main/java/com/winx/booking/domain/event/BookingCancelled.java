package com.winx.booking.domain.event;

public record BookingCancelled(Long bookingId, Long vehicleId) {
}
