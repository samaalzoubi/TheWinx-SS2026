package com.winx.rating.infrastructure.booking;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Resilience4j-guarded gateway towards bc03-booking. Deliberately a separate
 * Spring bean (rather than a private method on {@code
 * ResilientBookingClient}) so that the {@link CircuitBreaker} annotation is
 * applied through the Spring AOP proxy: calls arriving here always come from
 * a different bean, so the proxy - and therefore the circuit breaker - is
 * never bypassed by self-invocation.
 *
 * <p>A genuine HTTP 404 (booking id does not exist) is caught and translated
 * into {@link BookingLookupOutcome.NotFound} INSIDE the guarded method, i.e.
 * before the exception ever reaches the circuit breaker's AOP interceptor.
 * This means legitimate "no such booking" answers never count against the
 * breaker's failure-rate threshold. Only real faults (connection refused,
 * timeout, 5xx, or {@code CallNotPermittedException} once the breaker is
 * open) propagate out of this method and are routed to the fallback, which
 * never throws either - it returns {@link BookingLookupOutcome.Unavailable}.
 */
@Component
public class BookingLookupGateway {

    private static final Logger log = LoggerFactory.getLogger(BookingLookupGateway.class);

    private final BookingFeignClient bookingFeignClient;

    public BookingLookupGateway(BookingFeignClient bookingFeignClient) {
        this.bookingFeignClient = bookingFeignClient;
    }

    @CircuitBreaker(name = "bookingClient", fallbackMethod = "findBookingFallback")
    public BookingLookupOutcome findBooking(Long bookingId) {
        try {
            BookingFeignResponse booking = bookingFeignClient.getBooking(bookingId);
            return new BookingLookupOutcome.Found(booking);
        } catch (FeignException.NotFound e) {
            return new BookingLookupOutcome.NotFound();
        }
    }

    @SuppressWarnings("unused")
    private BookingLookupOutcome findBookingFallback(Long bookingId, Throwable t) {
        log.warn("Booking service unavailable while looking up booking {} ({}: {}); " +
                        "falling back to mock booking data",
                bookingId, t.getClass().getSimpleName(), t.getMessage());
        return new BookingLookupOutcome.Unavailable();
    }
}
