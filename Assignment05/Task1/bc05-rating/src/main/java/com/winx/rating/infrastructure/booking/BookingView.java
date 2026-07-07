package com.winx.rating.infrastructure.booking;

// we keep status as a plain String since Rating doesn't own the Booking lifecycle and only needs it to decide if feedback is allowed
public record BookingView(Long id, Long userId, Long vehicleId, Long providerId, String status) {

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
}
