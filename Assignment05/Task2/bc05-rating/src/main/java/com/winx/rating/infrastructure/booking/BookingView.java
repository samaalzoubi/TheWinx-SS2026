package com.winx.rating.infrastructure.booking;

// we kept status a plain String here, we don't own the Booking lifecycle, just need it to decide whether feedback is allowed
public record BookingView(Long id, Long userId, Long vehicleId, Long providerId, String status) {

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
}
