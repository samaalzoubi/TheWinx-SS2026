package com.winx.gateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "bc03-booking")
public interface BookingClient {

    @PostMapping("/api/bookings")
    BookingResponse create(@RequestBody CreateBookingRequest request);

    @PostMapping("/api/bookings/{id}/end")
    BookingResponse end(@PathVariable("id") Long id, @RequestBody EndBookingRequest request);

    @PostMapping("/api/bookings/{id}/cancel")
    BookingResponse cancel(@PathVariable("id") Long id);

    @GetMapping("/api/bookings/{id}")
    BookingResponse getBooking(@PathVariable("id") Long id);

    @GetMapping("/api/bookings/user/{userId}")
    List<BookingResponse> history(@PathVariable("userId") Long userId);

    @GetMapping("/api/bookings/vehicle/{vehicleId}")
    List<BookingResponse> byVehicle(@PathVariable("vehicleId") Long vehicleId);

    record CreateBookingRequest(Long userId, Long vehicleId) {
    }

    record EndBookingRequest(Double endLatitude, Double endLongitude, String paymentMethod) {
    }

    record BookingResponse(Long id, Long userId, Long vehicleId, String status, String startTime, String endTime,
                            Double startLatitude, Double startLongitude, Double endLatitude, Double endLongitude,
                            String vehicleType, BigDecimal pricePerUnit, String billingModel,
                            Double distanceKm, BigDecimal totalCost, Long paymentId, String paymentStatus) {
    }
}
