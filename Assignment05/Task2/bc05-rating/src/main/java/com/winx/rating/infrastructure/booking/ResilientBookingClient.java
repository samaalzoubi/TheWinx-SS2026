package com.winx.rating.infrastructure.booking;

import com.winx.rating.infrastructure.fleet.FleetLookupGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

// we don't need our own @CircuitBreaker here, both gateways we combine are already self-contained and never throw to us
@Component
@Primary
public class ResilientBookingClient implements BookingClient {

    private static final Logger log = LoggerFactory.getLogger(ResilientBookingClient.class);

    private final BookingLookupGateway bookingLookupGateway;
    private final FleetLookupGateway fleetLookupGateway;
    private final MockBookingClient fallbackClient;

    public ResilientBookingClient(BookingLookupGateway bookingLookupGateway,
                                   FleetLookupGateway fleetLookupGateway,
                                   MockBookingClient fallbackClient) {
        this.bookingLookupGateway = bookingLookupGateway;
        this.fleetLookupGateway = fleetLookupGateway;
        this.fallbackClient = fallbackClient;
    }

    @Override
    public Optional<BookingView> getBooking(Long bookingId) {
        BookingLookupOutcome outcome = bookingLookupGateway.findBooking(bookingId);

        if (outcome instanceof BookingLookupOutcome.Found found) {
            BookingFeignResponse booking = found.booking();
            Long providerId = fleetLookupGateway.resolveProviderId(booking.vehicleId());
            return Optional.of(new BookingView(booking.id(), booking.userId(), booking.vehicleId(), providerId, booking.status()));
        }

        if (outcome instanceof BookingLookupOutcome.NotFound) {
            return Optional.empty();
        }

        // we still resolve a fresh providerId via the Fleet gateway here instead of trusting the mock's baked-in one, Booking and Fleet are independent failure domains
        log.warn("Booking service unavailable for booking {} - degrading to MockBookingClient fallback data", bookingId);
        return fallbackClient.getBooking(bookingId)
                .map(mock -> new BookingView(mock.id(), mock.userId(), mock.vehicleId(),
                        fleetLookupGateway.resolveProviderId(mock.vehicleId()), mock.status()));
    }
}
