package com.winx.rating.infrastructure.booking;

/**
 * Read-only projection of a Booking as seen from the Rating bounded context.
 * Status is a plain String here (e.g. "ACTIVE" | "COMPLETED" | "CANCELLED")
 * on purpose: Rating does not own the Booking lifecycle, it only needs to
 * know the current status to decide whether feedback may be submitted.
 */
public record BookingView(Long id, Long userId, Long vehicleId, Long providerId, String status) {

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
}
