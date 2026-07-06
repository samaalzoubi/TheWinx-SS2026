package com.winx.rating.infrastructure.booking;

import com.winx.rating.infrastructure.fleet.FleetLookupGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Real {@link BookingClient} implementation, replacing {@link
 * MockBookingClient} as the primary bean now that Rating is wired into the
 * microservice architecture. Combines two independently circuit-broken
 * gateways to build a complete {@link BookingView}:
 *
 * <ul>
 *   <li>{@link BookingLookupGateway} - HTTP call to bc03-booking.</li>
 *   <li>{@link FleetLookupGateway} - HTTP call to bc02-fleet-management,
 *       used to resolve {@code providerId} from {@code vehicleId} since
 *       Booking's own representation does not carry a providerId.</li>
 * </ul>
 *
 * <p>This class intentionally carries NO {@code @CircuitBreaker} annotations
 * of its own: both gateways are self-contained (they never throw to their
 * caller, and internally guard their own Feign call via a Spring-proxied
 * bean, avoiding the self-invocation trap that would otherwise make
 * Resilience4j's AOP-based aspect a no-op). This class only has to
 * interpret their results.
 */
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

        // Unavailable: bc03-booking's circuit is open or otherwise unreachable.
        // Degrade to the same hardcoded example data used in standalone mode
        // rather than failing the whole rating submission outright. Booking
        // and Fleet are independent failure domains, so still attempt to
        // resolve a fresh providerId for the mock's vehicleId via the
        // (separately circuit-broken) Fleet gateway, instead of trusting the
        // mock's own baked-in providerId - this way a Fleet outage/recovery
        // is reflected even while Booking itself is degraded.
        log.warn("Booking service unavailable for booking {} - degrading to MockBookingClient fallback data", bookingId);
        return fallbackClient.getBooking(bookingId)
                .map(mock -> new BookingView(mock.id(), mock.userId(), mock.vehicleId(),
                        fleetLookupGateway.resolveProviderId(mock.vehicleId()), mock.status()));
    }
}
