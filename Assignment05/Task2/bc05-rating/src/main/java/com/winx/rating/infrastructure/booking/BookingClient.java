package com.winx.rating.infrastructure.booking;

import java.util.Optional;

// we kept MockBookingClient around as ResilientBookingClient's fallback, RatingSubmissionService only ever depends on this interface
public interface BookingClient {

    Optional<BookingView> getBooking(Long bookingId);
}
