package com.winx.booking.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Aggregate root of the Booking bounded context. Owns the full rental
 * lifecycle: search -> creation -> active ride -> completion (with cost
 * computation) or cancellation.
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Double startLatitude;

    @Column(nullable = false)
    private Double startLongitude;

    private Double endLatitude;

    private Double endLongitude;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Embedded
    private VehicleSnapshot vehicleSnapshot;

    @Embedded
    private RideSummary rideSummary = new RideSummary();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.ACTIVE;

    protected Booking() {
        // JPA
    }

    public Booking(Long userId, RideLocation startLocation, VehicleSnapshot vehicleSnapshot, LocalDateTime startTime) {
        this.userId = userId;
        this.startLatitude = startLocation.latitude();
        this.startLongitude = startLocation.longitude();
        this.vehicleSnapshot = vehicleSnapshot;
        this.startTime = startTime;
        this.status = BookingStatus.ACTIVE;
        this.rideSummary = new RideSummary();
    }

    public void complete(RideLocation endLocation, LocalDateTime endTime, double distanceKm, java.math.BigDecimal totalCost) {
        this.endLatitude = endLocation.latitude();
        this.endLongitude = endLocation.longitude();
        this.endTime = endTime;
        // Hibernate maps an @Embedded value whose columns are ALL null back to a
        // null field reference (rather than an instance with null fields), so a
        // freshly-reloaded ACTIVE booking may have rideSummary == null here.
        if (this.rideSummary == null) {
            this.rideSummary = new RideSummary();
        }
        this.rideSummary.setDistanceKm(distanceKm);
        this.rideSummary.setTotalCost(totalCost);
        this.status = BookingStatus.COMPLETED;
    }

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Double getStartLatitude() {
        return startLatitude;
    }

    public Double getStartLongitude() {
        return startLongitude;
    }

    public Double getEndLatitude() {
        return endLatitude;
    }

    public Double getEndLongitude() {
        return endLongitude;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public VehicleSnapshot getVehicleSnapshot() {
        return vehicleSnapshot;
    }

    public RideSummary getRideSummary() {
        // See note in complete(): a reloaded ACTIVE booking may have this null.
        return rideSummary != null ? rideSummary : new RideSummary();
    }

    public BookingStatus getStatus() {
        return status;
    }
}
