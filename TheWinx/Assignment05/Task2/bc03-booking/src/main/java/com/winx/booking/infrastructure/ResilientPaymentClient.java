package com.winx.booking.infrastructure;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Real HTTP-backed {@link PaymentClient} that calls the Payment service
 * (bc04) via Feign/Eureka, guarded by a Resilience4j circuit breaker named
 * {@code paymentClient}. Falls back to {@link MockPaymentClient} (always
 * succeeds) whenever the remote call fails technically (timeout, connection
 * refused, 5xx).
 */
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

    // Must NOT be private: resilience4j-spring invokes fallback methods via
    // reflection on the AOP proxy itself, and a private method bypasses the
    // proxy's target delegation, leaving injected fields null (see
    // https://github.com/resilience4j/resilience4j/issues/1993).
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
            // No payment recorded yet (e.g. booking still ACTIVE) - a legitimate
            // outcome, not a technical failure, so it must not trip the breaker.
            return null;
        }
    }

    // Must NOT be private - see note on chargeFallback above.
    public PaymentView findByBookingFallback(Long bookingId, Throwable t) {
        log.warn("Payment client circuit breaker fallback for booking {} lookup: {}", bookingId, t.toString());
        return fallbackClient.findByBooking(bookingId);
    }
}
