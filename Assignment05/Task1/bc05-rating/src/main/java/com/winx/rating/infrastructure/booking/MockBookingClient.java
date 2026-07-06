package com.winx.rating.infrastructure.booking;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hardcoded in-memory stand-in for a real Booking client, so BC-05 can be
 * demoed and tested completely standalone. Seeded with a handful of example
 * bookings covering both the "allowed" (COMPLETED) and "rejected"
 * (not yet COMPLETED) rating scenarios.
 */
@Component
public class MockBookingClient implements BookingClient {

    private static final Logger log = LoggerFactory.getLogger(MockBookingClient.class);

    private final Map<Long, BookingView> bookings = new ConcurrentHashMap<>();

    public MockBookingClient() {
        bookings.put(5001L, new BookingView(5001L, 1L, 1L, 1L, "COMPLETED"));
        bookings.put(5002L, new BookingView(5002L, 1L, 2L, 1L, "COMPLETED"));
        bookings.put(5003L, new BookingView(5003L, 2L, 3L, 2L, "ACTIVE"));
    }

    @PostConstruct
    void logSeedData() {
        log.info("MockBookingClient seeded with {} example bookings (standalone mode, no real Booking service):", bookings.size());
        bookings.values().forEach(b ->
                log.info("  booking {} -> userId={}, vehicleId={}, providerId={}, status={}",
                        b.id(), b.userId(), b.vehicleId(), b.providerId(), b.status()));
        log.info("Try: bookings 5001/5002 are COMPLETED (rating allowed), booking 5003 is ACTIVE (rating rejected with 409).");
    }

    @Override
    public Optional<BookingView> getBooking(Long bookingId) {
        return Optional.ofNullable(bookings.get(bookingId));
    }
}
