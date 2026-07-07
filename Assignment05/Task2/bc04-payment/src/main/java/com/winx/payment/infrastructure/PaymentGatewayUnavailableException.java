package com.winx.payment.infrastructure;

// we use this to simulate a technical gateway outage (not a normal decline) so we can demo the circuit breaker
public class PaymentGatewayUnavailableException extends RuntimeException {
    public PaymentGatewayUnavailableException(String message) {
        super(message);
    }
}
