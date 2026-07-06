package com.winx.rating.domain.event;

/**
 * Domain event raised when a new Rating is successfully submitted.
 * For this standalone service it is simply logged; a later integration
 * phase could publish it on a message broker without changing callers.
 */
public record RatingSubmitted(Long ratingId, Long bookingId, Integer vehicleScore, Integer providerScore) {
}
