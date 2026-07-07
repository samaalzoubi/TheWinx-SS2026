package com.winx.rating.domain.event;

// we just log this for now since there's no broker yet, but publishing it later won't require callers to change
public record RatingSubmitted(Long ratingId, Long bookingId, Integer vehicleScore, Integer providerScore) {
}
