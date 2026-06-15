package com.winx.booking.application;

import com.winx.booking.api.dto.BookingCreateRequest;
import com.winx.booking.api.dto.PrincipalDto;
import com.winx.booking.api.dto.VehicleDto;
import com.winx.booking.domain.Booking;
import com.winx.booking.domain.BookingStatus;
import com.winx.booking.domain.vo.RideLocation;
import com.winx.booking.domain.vo.VehicleSnapshot;
import com.winx.booking.exception.ActiveBookingExistsException;
import com.winx.booking.exception.BookingNotFoundException;
import com.winx.booking.exception.VehicleNotAvailableException;
import com.winx.booking.infrastructure.client.FleetGateway;
import com.winx.booking.infrastructure.client.IdentityGateway;
import com.winx.booking.infrastructure.persistence.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Orchestrates the booking use cases. Cross-context reads/writes go through the
 * circuit-breaker-protected gateways; this service never touches another service's database.
 *
 * <p>This pass implements Core CRUD (create / read / list / cancel). End-ride, cost computation,
 * restriction validation and the Payment trigger are deferred to a later pass.
 */
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository repository;
    private final IdentityGateway identityGateway;
    private final FleetGateway fleetGateway;

    /**
     * Creates an ACTIVE booking: authenticate the user, confirm the vehicle is AVAILABLE,
     * enforce one-active-booking-per-user, persist, then mark the vehicle BOOKED in Fleet.
     * If the status flip fails the whole transaction rolls back, so no orphan booking remains.
     */
    @Transactional
    public Booking createBooking(String authToken, BookingCreateRequest request) {
        PrincipalDto principal = identityGateway.validate(authToken);

        VehicleDto vehicle = fleetGateway.findVehicle(request.vehicleId());
        if (vehicle == null || !"AVAILABLE".equalsIgnoreCase(vehicle.status())) {
            throw new VehicleNotAvailableException(request.vehicleId());
        }

        repository.findFirstByUserIdAndStatus(principal.id(), BookingStatus.ACTIVE)
                .ifPresent(existing -> { throw new ActiveBookingExistsException(principal.id()); });

        VehicleSnapshot snapshot = new VehicleSnapshot(
                vehicle.vehicleId(), vehicle.type(), vehicle.pricePerUnit(), vehicle.billingModel());
        RideLocation start = new RideLocation(request.startLatitude(), request.startLongitude());

        Booking booking = repository.save(Booking.start(principal.id(), snapshot, start));
        fleetGateway.markBooked(vehicle.vehicleId());
        return booking;
    }

    /** Cancels an ACTIVE booking and releases the vehicle back to AVAILABLE. */
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        booking.cancel();
        fleetGateway.markAvailable(booking.getVehicleSnapshot().getVehicleId());
        return booking;
    }

    @Transactional(readOnly = true)
    public Booking findById(Long bookingId) {
        return repository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
    }

    @Transactional(readOnly = true)
    public List<Booking> findByUser(Long userId) {
        return repository.findByUserId(userId);
    }

    /** Delegates vehicle search to Fleet Management (used by the search UI). */
    @Transactional(readOnly = true)
    public List<VehicleDto> searchVehicles(double lat, double lon, double radiusKm,
                                           String type, BigDecimal maxPrice) {
        return fleetGateway.search(lat, lon, radiusKm, type, maxPrice);
    }
}
