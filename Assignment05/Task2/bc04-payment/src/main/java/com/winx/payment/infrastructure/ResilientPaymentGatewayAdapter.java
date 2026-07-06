package com.winx.payment.infrastructure;

import com.winx.payment.domain.model.Money;
import com.winx.payment.domain.model.PaymentMethod;
import com.winx.payment.domain.model.PaymentResult;
import com.winx.payment.domain.model.PaymentStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Wraps the (mock) external payment gateway call in a Resilience4j circuit
 * breaker ("paymentGateway", tuned in application.yml / the config server's
 * bc04-payment.yml). This is the one circuit breaker the Payment context
 * owns, per the team's Assignment 05 plan - Payment has no other bounded
 * context to call into, so it protects its own external-gateway boundary
 * instead. Marked {@code @Primary} so {@link com.winx.payment.domain.service.PaymentProcessingService}
 * transparently gets the protected version instead of {@link MockPaymentGatewayAdapter} directly.
 */
@Component
@Primary
public class ResilientPaymentGatewayAdapter implements PaymentGatewayAdapter {

    private static final Logger log = LoggerFactory.getLogger(ResilientPaymentGatewayAdapter.class);

    private final MockPaymentGatewayAdapter delegate;

    public ResilientPaymentGatewayAdapter(MockPaymentGatewayAdapter delegate) {
        this.delegate = delegate;
    }

    @Override
    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "chargeFallback")
    public PaymentResult charge(Money money, PaymentMethod method) {
        return delegate.charge(money, method);
    }

    @SuppressWarnings("unused") // invoked reflectively by the resilience4j aspect
    private PaymentResult chargeFallback(Money money, PaymentMethod method, Throwable throwable) {
        log.warn("Payment gateway circuit breaker fallback triggered: {}", throwable.toString());
        return new PaymentResult(PaymentStatus.FAILED, null,
                "Payment gateway unavailable (circuit breaker open): " + throwable.getMessage());
    }
}
