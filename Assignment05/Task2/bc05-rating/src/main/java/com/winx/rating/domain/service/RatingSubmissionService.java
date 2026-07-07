package com.winx.rating.domain.service;

import com.winx.rating.domain.event.RatingSubmitted;
import com.winx.rating.domain.exception.BookingNotCompletedException;
import com.winx.rating.domain.exception.BookingNotFoundException;
import com.winx.rating.domain.exception.DuplicateRatingException;
import com.winx.rating.domain.exception.InvalidScoreException;
import com.winx.rating.domain.model.Rating;
import com.winx.rating.domain.model.RatingTarget;
import com.winx.rating.domain.model.Review;
import com.winx.rating.domain.repository.RatingRepository;
import com.winx.rating.infrastructure.booking.BookingClient;
import com.winx.rating.infrastructure.booking.BookingView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RatingSubmissionService {

    private static final Logger log = LoggerFactory.getLogger(RatingSubmissionService.class);

    private static final int MIN_SCORE = 1;
    private static final int MAX_SCORE = 5;

    private final RatingRepository ratingRepository;
    private final BookingClient bookingClient;

    public RatingSubmissionService(RatingRepository ratingRepository, BookingClient bookingClient) {
        this.ratingRepository = ratingRepository;
        this.bookingClient = bookingClient;
    }

    @Transactional
    public Rating submitRating(Long bookingId, Long userId, Integer vehicleScore, Integer providerScore, String comment) {
        validateScore(vehicleScore, "vehicleScore");
        validateScore(providerScore, "providerScore");

        BookingView booking = bookingClient.getBooking(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (!booking.isCompleted()) {
            throw new BookingNotCompletedException(bookingId, booking.status());
        }

        if (ratingRepository.existsByRatingTarget_BookingId(bookingId)) {
            throw new DuplicateRatingException(bookingId);
        }

        RatingTarget ratingTarget = new RatingTarget(booking.vehicleId(), booking.providerId(), bookingId);
        Review review = new Review(vehicleScore, providerScore, comment);
        Rating rating = Rating.create(userId, ratingTarget, review);

        Rating saved = ratingRepository.save(rating);

        RatingSubmitted event = new RatingSubmitted(saved.getId(), bookingId, vehicleScore, providerScore);
        log.info("Domain event published: {}", event);

        return saved;
    }

    private void validateScore(Integer score, String fieldName) {
        if (score == null || score < MIN_SCORE || score > MAX_SCORE) {
            throw new InvalidScoreException(fieldName + " must be between " + MIN_SCORE + " and " + MAX_SCORE + " (got " + score + ")");
        }
    }
}
