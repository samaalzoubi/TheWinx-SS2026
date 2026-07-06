package com.winx.booking.domain.service;

import com.winx.booking.domain.event.BookingCancelled;
import com.winx.booking.domain.event.BookingCompleted;
import com.winx.booking.domain.event.BookingCreated;
import com.winx.booking.domain.event.PaymentTriggered;
import com.winx.booking.domain.exception.ActiveBookingExistsException;
import com.winx.booking.domain.exception.BookingNotFoundException;
import com.winx.booking.domain.exception.InvalidBookingStateException;
import com.winx.booking.domain.exception.UserNotFoundException;
import com.winx.booking.domain.exception.VehicleNotFoundException;
import com.winx.booking.domain.exception.VehicleUnavailableException;
import com.winx.booking.domain.model.Booking;
import com.winx.booking.domain.model.BookingStatus;
import com.winx.booking.domain.model.RideLocation;
import com.winx.booking.domain.model.VehicleSnapshot;
import com.winx.booking.domain.repository.BookingRepository;
import com.winx.booking.infrastructure.PaymentClient;
import com.winx.booking.infrastructure.PaymentOutcome;
import com.winx.booking.infrastructure.UserClient;
import com.winx.booking.infrastructure.UserView;
import com.winx.booking.infrastructure.VehicleClient;
import com.winx.booking.infrastructure.VehicleView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Application/domain service orchestrating the full booking lifecycle:
 * creation, ending a ride (with cost computation and payment trigger), and
 * cancellation.
 */
@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final UserClient userClient;
    private final VehicleClient vehicleClient;
    private final PaymentClient paymentClient;
    private final RestrictionValidator restrictionValidator;
    private final CostCalculationService costCalculationService;

    public BookingService(BookingRepository bookingRepository,
                           UserClient userClient,
                           VehicleClient vehicleClient,
                           PaymentClient paymentClient,
                           RestrictionValidator restrictionValidator,
                           CostCalculationService costCalculationService) {
        this.bookingRepository = bookingRepository;
        this.userClient = userClient;
        this.vehicleClient = vehicleClient;
        this.paymentClient = paymentClient;
        this.restrictionValidator = restrictionValidator;
        this.costCalculationService = costCalculationService;
    }

    @Transactional
    public Booking createBooking(Long userId, Long vehicleId) {
        UserView user = userClient.getUser(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        VehicleView vehicle = vehicleClient.getVehicle(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));

        if (!"AVAILABLE".equals(vehicle.status())) {
            throw new VehicleUnavailableException(vehicleId);
        }

        bookingRepository.findByUserIdAndStatus(userId, BookingStatus.ACTIVE)
                .ifPresent(b -> {
                    throw new ActiveBookingExistsException(userId);
                });

        restrictionValidator.validate(user, vehicle);

        VehicleSnapshot snapshot = new VehicleSnapshot(
                vehicle.id(), vehicle.type(), vehicle.pricePerUnit(), vehicle.billingModel());
        RideLocation startLocation = new RideLocation(vehicle.latitude(), vehicle.longitude());

        Booking booking = new Booking(userId, startLocation, snapshot, LocalDateTime.now());
        booking = bookingRepository.save(booking);

        vehicleClient.updateStatus(vehicleId, "BOOKED");

        log.info("Domain event: {}", new BookingCreated(booking.getId(), userId, vehicleId));

        return booking;
    }

    @Transactional
    public BookingEndResult endBooking(Long bookingId, double endLatitude, double endLongitude, String paymentMethod) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new InvalidBookingStateException("Booking " + bookingId + " is not ACTIVE and cannot be ended");
        }

        LocalDateTime endTime = LocalDateTime.now();
        if (!endTime.isAfter(booking.getStartTime())) {
            endTime = booking.getStartTime().plusSeconds(1);
        }

        double distanceKm = costCalculationService.computeDistanceKm(
                booking.getStartLatitude(), booking.getStartLongitude(), endLatitude, endLongitude);
        BigDecimal totalCost = costCalculationService.computeCost(
                booking.getVehicleSnapshot(), booking.getStartTime(), endTime, distanceKm);

        booking.complete(new RideLocation(endLatitude, endLongitude), endTime, distanceKm, totalCost);
        booking = bookingRepository.save(booking);

        Long vehicleId = booking.getVehicleSnapshot().getVehicleId();
        vehicleClient.updateStatus(vehicleId, "AVAILABLE");

        log.info("Domain event: {}", new BookingCompleted(booking.getId(), totalCost));
        log.info("Domain event: {}", new PaymentTriggered(booking.getId(), totalCost, booking.getUserId()));

        PaymentOutcome paymentOutcome = paymentClient.charge(booking.getId(), booking.getUserId(), totalCost, paymentMethod);

        return new BookingEndResult(booking, paymentOutcome);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new InvalidBookingStateException("Booking " + bookingId + " is not ACTIVE and cannot be cancelled");
        }

        booking.cancel();
        booking = bookingRepository.save(booking);

        Long vehicleId = booking.getVehicleSnapshot().getVehicleId();
        vehicleClient.updateStatus(vehicleId, "AVAILABLE");

        log.info("Domain event: {}", new BookingCancelled(booking.getId(), vehicleId));

        return booking;
    }
}
