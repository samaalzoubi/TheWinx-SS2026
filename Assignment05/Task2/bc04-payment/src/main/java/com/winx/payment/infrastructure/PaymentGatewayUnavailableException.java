package com.winx.payment.infrastructure;

/** Thrown by {@link MockPaymentGatewayAdapter} to simulate a technical gateway outage (as opposed to a normal card decline), for demonstrating the {@link ResilientPaymentGatewayAdapter} circuit breaker. */
public class PaymentGatewayUnavailableException extends RuntimeException {
    public PaymentGatewayUnavailableException(String message) {
        super(message);
    }
}
