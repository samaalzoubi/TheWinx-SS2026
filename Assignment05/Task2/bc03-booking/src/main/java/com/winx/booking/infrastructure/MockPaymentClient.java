package com.winx.booking.infrastructure;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

// we made this always succeed to keep the standalone demo flow simple, real failure handling lives in the actual Payment service
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
        // we never persist what we "charge" here, so null (unknown) is the only honest answer for a lookup
        return null;
    }
}
