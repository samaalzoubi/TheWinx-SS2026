package com.winx.booking.domain.service;

import com.winx.booking.domain.model.Booking;
import com.winx.booking.infrastructure.PaymentOutcome;

// we bundle the payment outcome in here instead of persisting it on Booking, it only needs to flow through to the API response
public record BookingEndResult(Booking booking, PaymentOutcome paymentOutcome) {
}
