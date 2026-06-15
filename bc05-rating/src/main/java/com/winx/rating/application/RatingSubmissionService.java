package com.winx.rating.application;

import com.winx.rating.domain.Rating;
import com.winx.rating.domain.RatingTarget;
import com.winx.rating.domain.Review;
import com.winx.rating.domain.Score;
import com.winx.rating.infrastructure.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RatingSubmissionService {

    private final RatingRepository ratingRepository;

    /**
     * Submits a rating for a completed booking.
     * Task 1: booking completion is trusted (mocked). Task 2 will verify via BookingFeignClient.
     * Invariants enforced:
     *  - scores must be 1–5 (enforced by Score.of)
     *  - exactly one rating per bookingId
     */
    public Rating submitRating(Long bookingId, Long userId,
                               Long vehicleId, Long providerId,
                               int vehicleScore, int providerScore,
                               String comment) {
        if (ratingRepository.existsByTarget_BookingId(bookingId)) {
            throw new IllegalStateException(
                "Booking " + bookingId + " has already been rated.");
        }

        RatingTarget target = new RatingTarget(vehicleId, providerId, bookingId);
        Review review = new Review(Score.of(vehicleScore), Score.of(providerScore), comment);
        return ratingRepository.save(Rating.create(userId, target, review));
    }
}
