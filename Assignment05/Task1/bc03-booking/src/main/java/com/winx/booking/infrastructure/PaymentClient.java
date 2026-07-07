package com.winx.booking.infrastructure;

import java.math.BigDecimal;

// we kept this an interface so a real HTTP-backed implementation can slot in later without touching any calling code
public interface PaymentClient {

    PaymentOutcome charge(Long bookingId, Long userId, BigDecimal amount, String paymentMethod);

    // this is read-only, for display only, never used to drive booking state - null means unknown, whether that's no payment yet or Payment being unreachable
    PaymentView findByBooking(Long bookingId);
}
