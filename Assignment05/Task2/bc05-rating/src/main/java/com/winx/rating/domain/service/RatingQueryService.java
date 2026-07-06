package com.winx.rating.domain.service;

import com.winx.rating.domain.exception.RatingNotFoundException;
import com.winx.rating.domain.model.Rating;
import com.winx.rating.domain.repository.RatingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class RatingQueryService {

    private final RatingRepository ratingRepository;

    public RatingQueryService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    public Rating getById(Long id) {
        return ratingRepository.findById(id)
                .orElseThrow(() -> new RatingNotFoundException(id));
    }

    public List<Rating> getVehicleRatings(Long vehicleId) {
        return ratingRepository.findByRatingTarget_VehicleId(vehicleId);
    }

    public List<Rating> getProviderRatings(Long providerId) {
        return ratingRepository.findByRatingTarget_ProviderId(providerId);
    }

    public Optional<Rating> getRatingForBooking(Long bookingId) {
        return ratingRepository.findByRatingTarget_BookingId(bookingId);
    }

    public double getAverageScore(Long vehicleId) {
        List<Rating> ratings = getVehicleRatings(vehicleId);
        if (ratings.isEmpty()) {
            return 0.0;
        }
        double sum = ratings.stream()
                .mapToInt(r -> r.getReview().getVehicleScore())
                .sum();
        return sum / ratings.size();
    }
}
