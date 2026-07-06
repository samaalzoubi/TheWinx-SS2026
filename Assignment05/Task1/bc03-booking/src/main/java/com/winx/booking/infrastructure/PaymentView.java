package com.winx.booking.infrastructure;

import java.math.BigDecimal;

/**
 * Read-only view of a payment as seen from the Booking context, fetched live
 * from the Payment bounded context (bc04). {@code null} from
 * {@link PaymentClient#findByBooking} means "unknown" (no payment recorded
 * yet, or the Payment service is unreachable) - the two are deliberately not
 * distinguished here since the UI treats them the same way.
 */
public record PaymentView(Long paymentId, String status, String failureReason, BigDecimal amount, String paidAt) {
}
