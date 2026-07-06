package com.winx.rating.api;

import com.winx.rating.api.dto.AverageScoreResponse;
import com.winx.rating.api.dto.CreateRatingRequest;
import com.winx.rating.api.dto.RatingDto;
import com.winx.rating.domain.exception.RatingNotFoundException;
import com.winx.rating.domain.model.Rating;
import com.winx.rating.domain.service.RatingQueryService;
import com.winx.rating.domain.service.RatingSubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingSubmissionService submissionService;
    private final RatingQueryService queryService;

    public RatingController(RatingSubmissionService submissionService, RatingQueryService queryService) {
        this.submissionService = submissionService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<RatingDto> submitRating(@Valid @RequestBody CreateRatingRequest request) {
        Rating rating = submissionService.submitRating(
                request.bookingId(),
                request.userId(),
                request.vehicleScore(),
                request.providerScore(),
                request.comment()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(RatingDto.from(rating));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RatingDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(RatingDto.from(queryService.getById(id)));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<RatingDto> getByBooking(@PathVariable Long bookingId) {
        return queryService.getRatingForBooking(bookingId)
                .map(rating -> ResponseEntity.ok(RatingDto.from(rating)))
                .orElseThrow(() -> new RatingNotFoundException(bookingId));
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<RatingDto>> getByVehicle(@PathVariable Long vehicleId) {
        List<RatingDto> dtos = queryService.getVehicleRatings(vehicleId).stream()
                .map(RatingDto::from)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<RatingDto>> getByProvider(@PathVariable Long providerId) {
        List<RatingDto> dtos = queryService.getProviderRatings(providerId).stream()
                .map(RatingDto::from)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/vehicle/{vehicleId}/average")
    public ResponseEntity<AverageScoreResponse> getAverage(@PathVariable Long vehicleId) {
        int count = queryService.getVehicleRatings(vehicleId).size();
        double average = queryService.getAverageScore(vehicleId);
        return ResponseEntity.ok(new AverageScoreResponse(vehicleId, average, count));
    }
}
