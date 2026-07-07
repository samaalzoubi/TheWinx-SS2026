package com.winx.booking.infrastructure;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// we fall back to MockPaymentClient here whenever the real call fails technically (timeout, connection refused, 5xx)
@Component
@Primary
public class ResilientPaymentClient implements PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(ResilientPaymentClient.class);

    private final PaymentFeignClient feignClient;
    private final MockPaymentClient fallbackClient;

    public ResilientPaymentClient(PaymentFeignClient feignClient, MockPaymentClient fallbackClient) {
        this.feignClient = feignClient;
        this.fallbackClient = fallbackClient;
    }

    @Override
    @CircuitBreaker(name = "paymentClient", fallbackMethod = "chargeFallback")
    public PaymentOutcome charge(Long bookingId, Long userId, BigDecimal amount, String paymentMethod) {
        PaymentFeignClient.PaymentFeignRequest request = new PaymentFeignClient.PaymentFeignRequest(
                bookingId, userId, amount, "EUR", paymentMethod, "****-" + paymentMethod);
        PaymentFeignClient.PaymentFeignResponse response = feignClient.charge(request);
        return new PaymentOutcome(response.id(), response.status());
    }

    // we kept this non-private since resilience4j-spring invokes fallbacks via reflection on the AOP proxy, a private method would bypass that and leave our injected fields null
    public PaymentOutcome chargeFallback(Long bookingId, Long userId, BigDecimal amount, String paymentMethod, Throwable t) {
        log.warn("Payment client circuit breaker fallback for booking {}: {}", bookingId, t.toString());
        return fallbackClient.charge(bookingId, userId, amount, paymentMethod);
    }

    @Override
    @CircuitBreaker(name = "paymentClient", fallbackMethod = "findByBookingFallback")
    public PaymentView findByBooking(Long bookingId) {
        try {
            PaymentFeignClient.PaymentFeignResponse r = feignClient.getByBooking(bookingId);
            return new PaymentView(r.id(), r.status(), r.failureReason(), r.amount(), r.paidAt());
        } catch (FeignException.NotFound e) {
            // we treat this as a legitimate outcome (e.g. booking still ACTIVE), not a technical failure, so it shouldn't trip the breaker
            return null;
        }
    }

    // non-private for the same reflection reason as chargeFallback above
    public PaymentView findByBookingFallback(Long bookingId, Throwable t) {
        log.warn("Payment client circuit breaker fallback for booking {} lookup: {}", bookingId, t.toString());
        return fallbackClient.findByBooking(bookingId);
    }
}
