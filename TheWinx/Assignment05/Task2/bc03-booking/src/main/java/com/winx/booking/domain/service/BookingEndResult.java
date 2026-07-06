package com.winx.booking.domain.service;

import com.winx.booking.domain.model.Booking;
import com.winx.booking.infrastructure.PaymentOutcome;

/**
 * Result of ending a booking: the (now completed) booking plus the payment
 * outcome returned by the {@link com.winx.booking.infrastructure.PaymentClient}.
 * The payment outcome is not necessarily persisted on the Booking entity
 * itself - it only needs to flow through to the API response.
 */
public record BookingEndResult(Booking booking, PaymentOutcome paymentOutcome) {
}
