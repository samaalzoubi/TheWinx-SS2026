package com.winx.booking.domain;

import com.winx.booking.domain.vo.RideLocation;
import com.winx.booking.domain.vo.RideSummary;
import com.winx.booking.domain.vo.TimeInterval;
import com.winx.booking.domain.vo.VehicleSnapshot;
import com.winx.booking.exception.InvalidBookingStateException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Booking aggregate root. Owns the rental lifecycle and enforces its invariants.
 * State transitions go through the methods here (never by setting the status field
 * directly) so the rules stay in one place and a state-history (Memento) caretaker
 * can be added later without touching callers.
 *
 * <p>Invariants enforced here:
 * <ul>
 *   <li>cancel/complete are only legal from ACTIVE;</li>
 *   <li>a COMPLETED booking can never become CANCELLED;</li>
 *   <li>endTime is set only when the ride completes.</li>
 * </ul>
 * (Cost computation and restriction validation live in services, in a later pass.)
 */
@Entity
@Table(name = "bookings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(nullable = false)
    private Long userId;

    @Embedded
    private VehicleSnapshot vehicleSnapshot;

    @Embedded
    private TimeInterval interval;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "start_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "start_longitude"))
    })
    private RideLocation startLocation;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "end_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "end_longitude"))
    })
    private RideLocation endLocation;

    @Embedded
    private RideSummary summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    private Booking(Long userId, VehicleSnapshot vehicleSnapshot, RideLocation startLocation) {
        this.userId = userId;
        this.vehicleSnapshot = vehicleSnapshot;
        this.startLocation = startLocation;
        this.interval = TimeInterval.startingNow();
        this.status = BookingStatus.ACTIVE;
    }

    /** Starts a new ACTIVE booking. The vehicle snapshot fixes the price at booking time. */
    public static Booking start(Long userId, VehicleSnapshot vehicleSnapshot, RideLocation startLocation) {
        return new Booking(userId, vehicleSnapshot, startLocation);
    }

    /** Cancels an ACTIVE booking. Illegal once COMPLETED or already CANCELLED. */
    public void cancel() {
        if (status != BookingStatus.ACTIVE) {
            throw new InvalidBookingStateException(
                    "Only an ACTIVE booking can be cancelled (current status: " + status + ").");
        }
        this.status = BookingStatus.CANCELLED;
    }

    /**
     * Completes an ACTIVE ride. Cost computation that produces the {@link RideSummary}
     * is wired up in the end-ride pass; this method only guards the transition.
     */
    public void complete(RideLocation endLocation, RideSummary summary, LocalDateTime endTime) {
        if (status != BookingStatus.ACTIVE) {
            throw new InvalidBookingStateException(
                    "Only an ACTIVE booking can be completed (current status: " + status + ").");
        }
        this.endLocation = endLocation;
        this.summary = summary;
        this.interval = this.interval.endingAt(endTime);
        this.status = BookingStatus.COMPLETED;
    }
}
