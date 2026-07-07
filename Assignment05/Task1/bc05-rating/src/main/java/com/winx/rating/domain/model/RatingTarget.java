package com.winx.rating.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class RatingTarget {

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    protected RatingTarget() {
    }

    public RatingTarget(Long vehicleId, Long providerId, Long bookingId) {
        this.vehicleId = Objects.requireNonNull(vehicleId, "vehicleId must not be null");
        this.providerId = Objects.requireNonNull(providerId, "providerId must not be null");
        this.bookingId = Objects.requireNonNull(bookingId, "bookingId must not be null");
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RatingTarget that)) return false;
        return Objects.equals(vehicleId, that.vehicleId)
                && Objects.equals(providerId, that.providerId)
                && Objects.equals(bookingId, that.bookingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleId, providerId, bookingId);
    }
}
