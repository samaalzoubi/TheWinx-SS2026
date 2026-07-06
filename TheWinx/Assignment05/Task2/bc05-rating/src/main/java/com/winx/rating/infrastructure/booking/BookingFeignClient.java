package com.winx.rating.infrastructure.booking;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Declarative HTTP client towards bc03-booking, resolved via Eureka service
 * discovery (load-balanced by name, no hardcoded host/port).
 *
 * <p>The optional {@code booking.service.url} property is left empty by
 * default, which keeps normal discovery-based resolution; it exists purely
 * so an operator/tester can pin this one client to a fixed URL (bypassing
 * Eureka) when verifying this integration in isolation from other
 * discovered services.
 */
@FeignClient(name = "bc03-booking", url = "${booking.service.url:}")
public interface BookingFeignClient {

    @GetMapping("/api/bookings/{id}")
    BookingFeignResponse getBooking(@PathVariable("id") Long id);
}
