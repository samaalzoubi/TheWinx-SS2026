package com.winx.rating.infrastructure.booking;

import java.util.Optional;

// we kept this as an interface so a real HTTP client can swap in later without touching RatingSubmissionService - MockBookingClient is the only implementation for now
public interface BookingClient {

    Optional<BookingView> getBooking(Long bookingId);
}
