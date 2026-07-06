package com.winx.rating.infrastructure.booking;

import java.util.Optional;

/**
 * Anti-corruption-layer style port towards the Booking bounded context.
 *
 * <p>This service runs completely standalone (Assignment "Task 1"), so the
 * only implementation available here is {@link MockBookingClient}, backed by
 * hardcoded in-memory example data. A later integration phase can add a real
 * HTTP-based implementation (e.g. calling bc03-booking's REST API) that
 * implements this same interface, without any change to
 * {@code RatingSubmissionService} or any other caller.
 */
public interface BookingClient {

    Optional<BookingView> getBooking(Long bookingId);
}
