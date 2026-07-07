package com.winx.rating.domain.event;

// we just log this for now, a later phase could publish it to a broker without changing any callers
public record RatingSubmitted(Long ratingId, Long bookingId, Integer vehicleScore, Integer providerScore) {
}
