package com.winx.rating.api.dto;

import com.winx.rating.domain.model.Rating;

import java.time.LocalDateTime;

public record RatingDto(
        Long id,
        Long userId,
        Long bookingId,
        Long vehicleId,
        Long providerId,
        Integer vehicleScore,
        Integer providerScore,
        String comment,
        LocalDateTime createdAt
) {

    public static RatingDto from(Rating rating) {
        return new RatingDto(
                rating.getId(),
                rating.getUserId(),
                rating.getRatingTarget().getBookingId(),
                rating.getRatingTarget().getVehicleId(),
                rating.getRatingTarget().getProviderId(),
                rating.getReview().getVehicleScore().getValue(),
                rating.getReview().getProviderScore().getValue(),
                rating.getReview().getComment(),
                rating.getCreatedAt()
        );
    }
}
