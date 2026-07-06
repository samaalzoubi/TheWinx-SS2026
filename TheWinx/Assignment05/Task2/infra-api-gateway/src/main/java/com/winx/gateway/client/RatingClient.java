package com.winx.gateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "bc05-rating")
public interface RatingClient {

    @PostMapping("/api/ratings")
    RatingResponse submit(@RequestBody SubmitRatingRequest request);

    @GetMapping("/api/ratings/booking/{bookingId}")
    RatingResponse getByBooking(@PathVariable("bookingId") Long bookingId);

    @GetMapping("/api/ratings/vehicle/{vehicleId}/average")
    AverageResponse getVehicleAverage(@PathVariable("vehicleId") Long vehicleId);

    @GetMapping("/api/ratings/provider/{providerId}")
    List<RatingResponse> getByProvider(@PathVariable("providerId") Long providerId);

    record SubmitRatingRequest(Long bookingId, Long userId, Integer vehicleScore, Integer providerScore, String comment) {
    }

    record RatingResponse(Long id, Long userId, Long bookingId, Long vehicleId, Long providerId,
                           Integer vehicleScore, Integer providerScore, String comment, String createdAt) {
    }

    record AverageResponse(Long vehicleId, Double averageScore, Integer count) {
    }
}
