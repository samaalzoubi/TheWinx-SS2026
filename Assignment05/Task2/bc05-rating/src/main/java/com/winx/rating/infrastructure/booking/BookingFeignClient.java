package com.winx.rating.infrastructure.booking;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// we left booking.service.url empty by default so Eureka resolves this normally, it's just there for pinning to a fixed URL when testing in isolation
@FeignClient(name = "bc03-booking", url = "${booking.service.url:}")
public interface BookingFeignClient {

    @GetMapping("/api/bookings/{id}")
    BookingFeignResponse getBooking(@PathVariable("id") Long id);
}
