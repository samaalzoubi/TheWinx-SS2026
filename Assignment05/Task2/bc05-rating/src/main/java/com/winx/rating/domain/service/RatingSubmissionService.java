package com.winx.rating.domain.service;

import com.winx.rating.domain.event.RatingSubmitted;
import com.winx.rating.domain.exception.BookingNotCompletedException;
import com.winx.rating.domain.exception.BookingNotFoundException;
import com.winx.rating.domain.exception.DuplicateRatingException;
import com.winx.rating.domain.model.Rating;
import com.winx.rating.domain.model.RatingTarget;
import com.winx.rating.domain.model.Review;
import com.winx.rating.domain.model.Score;
import com.winx.rating.domain.repository.RatingRepository;
import com.winx.rating.infrastructure.booking.BookingClient;
import com.winx.rating.infrastructure.booking.BookingView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RatingSubmissionService {

    private static final Logger log = LoggerFactory.getLogger(RatingSubmissionService.class);

    private final RatingRepository ratingRepository;
    private final BookingClient bookingClient;

    public RatingSubmissionService(RatingRepository ratingRepository, BookingClient bookingClient) {
        this.ratingRepository = ratingRepository;
        this.bookingClient = bookingClient;
    }

    @Transactional
    public Rating submitRating(Long bookingId, Long userId, Integer vehicleScore, Integer providerScore, String comment) {
        Score vehicleScoreVo = Score.of("vehicleScore", vehicleScore);
        Score providerScoreVo = Score.of("providerScore", providerScore);

        BookingView booking = bookingClient.getBooking(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (!booking.isCompleted()) {
            throw new BookingNotCompletedException(bookingId, booking.status());
        }

        if (ratingRepository.existsByRatingTarget_BookingId(bookingId)) {
            throw new DuplicateRatingException(bookingId);
        }

        RatingTarget ratingTarget = new RatingTarget(booking.vehicleId(), booking.providerId(), bookingId);
        Review review = new Review(vehicleScoreVo, providerScoreVo, comment);
        Rating rating = Rating.create(userId, ratingTarget, review);

        Rating saved;
        try {
            saved = ratingRepository.save(rating);
        } catch (DataIntegrityViolationException e) {
            // the existsBy check above isn't atomic, this catches the rare race where two submissions for the
            // same booking land at once, the DB's unique constraint on booking_id is the real guarantee
            throw new DuplicateRatingException(bookingId);
        }

        RatingSubmitted event = new RatingSubmitted(saved.getId(), bookingId, vehicleScore, providerScore);
        log.info("Domain event published: {}", event);

        return saved;
    }
}
