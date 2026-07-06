package com.winx.booking.infrastructure;

import java.math.BigDecimal;

/**
 * Gateway to the Payment bounded context. Implemented today by
 * {@link MockPaymentClient} which always succeeds, so that this service
 * runs fully standalone; a later integration phase can add a real
 * HTTP-based implementation without touching any calling code.
 */
public interface PaymentClient {

    PaymentOutcome charge(Long bookingId, Long userId, BigDecimal amount, String paymentMethod);

    /**
     * Read-only lookup of a booking's payment, for display purposes only
     * (never used to drive booking state). Returns {@code null} if unknown -
     * either no payment recorded yet or the Payment service is unreachable.
     */
    PaymentView findByBooking(Long bookingId);
}
