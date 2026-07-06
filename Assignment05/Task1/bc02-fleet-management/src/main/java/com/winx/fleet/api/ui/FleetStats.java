package com.winx.fleet.api.ui;

import com.winx.fleet.domain.model.Vehicle;
import com.winx.fleet.domain.model.VehicleStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Small read-only aggregate that feeds the stat tiles on the UI pages
 * (home, browse). This is purely a view-layer convenience computed from
 * whatever vehicle list the calling UI controller already fetched - it is
 * not part of the domain model.
 */
public final class FleetStats {

    private final long total;
    private final long available;
    private final long booked;
    private final BigDecimal averagePrice;

    private FleetStats(long total, long available, long booked, BigDecimal averagePrice) {
        this.total = total;
        this.available = available;
        this.booked = booked;
        this.averagePrice = averagePrice;
    }

    public static FleetStats of(List<Vehicle> vehicles) {
        long available = vehicles.stream().filter(v -> v.getStatus() == VehicleStatus.AVAILABLE).count();
        long booked = vehicles.stream().filter(v -> v.getStatus() == VehicleStatus.BOOKED).count();

        OptionalDouble avg = vehicles.stream()
                .filter(v -> v.getPricingPolicy() != null && v.getPricingPolicy().getPricePerUnit() != null)
                .mapToDouble(v -> v.getPricingPolicy().getPricePerUnit().doubleValue())
                .average();
        BigDecimal averagePrice = avg.isPresent()
                ? BigDecimal.valueOf(avg.getAsDouble()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);

        return new FleetStats(vehicles.size(), available, booked, averagePrice);
    }

    public long getTotal() {
        return total;
    }

    public long getAvailable() {
        return available;
    }

    public long getBooked() {
        return booked;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }
}
