package com.winx.booking.infrastructure;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory stand-in for the Payment bounded context. Always succeeds, to
 * keep the standalone booking demo flow simple; real payment failure
 * handling is exercised inside the actual Payment service itself.
 */
@Component
public class MockPaymentClient implements PaymentClient {

    private final AtomicLong paymentIdSequence = new AtomicLong(1000);

    @Override
    public PaymentOutcome charge(Long bookingId, Long userId, BigDecimal amount, String paymentMethod) {
        long paymentId = paymentIdSequence.getAndIncrement();
        return new PaymentOutcome(paymentId, "PAID");
    }

    @Override
    public PaymentView findByBooking(Long bookingId) {
        // This mock never persists anything it "charges", so it has nothing
        // truthful to report back for a lookup - unknown is the honest answer.
        return null;
    }
}
