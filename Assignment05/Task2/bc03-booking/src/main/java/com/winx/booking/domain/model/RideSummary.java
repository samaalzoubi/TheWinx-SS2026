package com.winx.booking.domain.model;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.Objects;

// both fields stay null until the ride actually completes
@Embeddable
public class RideSummary {

    private Double distanceKm;
    private BigDecimal totalCost;

    public RideSummary() {
        // JPA / default (not-yet-completed) state
    }

    public RideSummary(Double distanceKm, BigDecimal totalCost) {
        this.distanceKm = distanceKm;
        this.totalCost = totalCost;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RideSummary that)) return false;
        return Objects.equals(distanceKm, that.distanceKm) && Objects.equals(totalCost, that.totalCost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(distanceKm, totalCost);
    }
}
