package com.winx.rating.domain.repository;

import com.winx.rating.domain.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByRatingTarget_BookingId(Long bookingId);

    List<Rating> findByRatingTarget_VehicleId(Long vehicleId);

    List<Rating> findByRatingTarget_ProviderId(Long providerId);

    boolean existsByRatingTarget_BookingId(Long bookingId);
}
