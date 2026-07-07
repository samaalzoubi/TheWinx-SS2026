package com.winx.rating.infrastructure.booking;

// we modelled this explicitly instead of Optional/null so callers can tell "booking genuinely doesn't exist" apart from "Booking is unreachable"
public sealed interface BookingLookupOutcome {

    record Found(BookingFeignResponse booking) implements BookingLookupOutcome {
    }

    record NotFound() implements BookingLookupOutcome {
    }

    record Unavailable() implements BookingLookupOutcome {
    }
}
