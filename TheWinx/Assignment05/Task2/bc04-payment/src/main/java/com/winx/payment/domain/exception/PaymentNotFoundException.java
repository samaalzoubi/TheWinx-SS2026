package com.winx.payment.domain.exception;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(String message) {
        super(message);
    }

    public static PaymentNotFoundException forId(Long id) {
        return new PaymentNotFoundException("Payment not found: " + id);
    }

    public static PaymentNotFoundException forBookingId(Long bookingId) {
        return new PaymentNotFoundException("No payment found for booking: " + bookingId);
    }
}
