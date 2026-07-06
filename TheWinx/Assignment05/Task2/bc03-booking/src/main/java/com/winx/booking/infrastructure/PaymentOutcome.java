package com.winx.booking.infrastructure;

/**
 * Result of a payment charge attempt as seen from the Booking context.
 * Sourced from the Payment bounded context (mocked for now).
 */
public record PaymentOutcome(Long paymentId, String status) {
}
