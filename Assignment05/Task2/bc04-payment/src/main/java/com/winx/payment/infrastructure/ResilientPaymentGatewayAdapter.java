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

// this is the one circuit breaker Payment owns, since it has no other bounded context to call into, just its own gateway boundary
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
