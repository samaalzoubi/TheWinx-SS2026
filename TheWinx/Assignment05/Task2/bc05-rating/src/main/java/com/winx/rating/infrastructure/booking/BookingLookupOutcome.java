package com.winx.rating.infrastructure.booking;

/**
 * Result of a {@link BookingLookupGateway#findBooking(Long)} call, modelled
 * explicitly (rather than as a bare {@code Optional} or a null/sentinel
 * value) so callers can tell apart two very different situations that both
 * "have no booking to show":
 *
 * <ul>
 *   <li>{@link NotFound} - Booking answered normally and confirmed the
 *       booking id does not exist. This is a legitimate business outcome,
 *       not a fault, and must NOT be treated as a circuit-breaker failure or
 *       trigger a fallback to mock data.</li>
 *   <li>{@link Unavailable} - Booking could not be reached at all (circuit
 *       open, connection failure, timeout, ...). The caller should degrade
 *       gracefully, e.g. by falling back to {@link MockBookingClient}.</li>
 * </ul>
 */
public sealed interface BookingLookupOutcome {

    record Found(BookingFeignResponse booking) implements BookingLookupOutcome {
    }

    record NotFound() implements BookingLookupOutcome {
    }

    record Unavailable() implements BookingLookupOutcome {
    }
}
