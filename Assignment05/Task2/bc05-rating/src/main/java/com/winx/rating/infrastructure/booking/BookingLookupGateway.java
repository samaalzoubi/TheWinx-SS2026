package com.winx.rating.infrastructure.booking;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// we made this its own bean, not a private method on ResilientBookingClient, so the circuit breaker's AOP proxy never gets bypassed by self-invocation
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
