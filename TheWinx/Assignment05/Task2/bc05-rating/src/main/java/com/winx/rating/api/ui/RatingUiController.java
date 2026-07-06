package com.winx.rating.api.ui;

import com.winx.rating.domain.exception.BookingNotCompletedException;
import com.winx.rating.domain.exception.BookingNotFoundException;
import com.winx.rating.domain.exception.DuplicateRatingException;
import com.winx.rating.domain.exception.InvalidScoreException;
import com.winx.rating.domain.model.Rating;
import com.winx.rating.domain.repository.RatingRepository;
import com.winx.rating.domain.service.RatingQueryService;
import com.winx.rating.domain.service.RatingSubmissionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Server-rendered UI on top of the same domain services used by the REST
 * API - kept deliberately simple for demo purposes. The browse-all page
 * additionally reads straight from {@link RatingRepository} since it needs
 * an unfiltered "list everything" view that the query service does not
 * otherwise expose; this is UI-layer-only wiring and does not change the
 * REST contract or domain services.
 */
@Controller
@RequestMapping("/ui")
public class RatingUiController {

    private final RatingSubmissionService submissionService;
    private final RatingQueryService queryService;
    private final RatingRepository ratingRepository;

    public RatingUiController(RatingSubmissionService submissionService,
                               RatingQueryService queryService,
                               RatingRepository ratingRepository) {
        this.submissionService = submissionService;
        this.queryService = queryService;
        this.ratingRepository = ratingRepository;
    }

    @GetMapping
    public String home() {
        return "ui/home";
    }

    @GetMapping("/rate")
    public String rateForm(Model model) {
        if (!model.containsAttribute("vehicleScore")) {
            model.addAttribute("vehicleScore", 5);
            model.addAttribute("providerScore", 5);
        }
        return "ui/rate";
    }

    @PostMapping("/rate")
    public String submitRating(@RequestParam Long bookingId,
                                @RequestParam Long userId,
                                @RequestParam Integer vehicleScore,
                                @RequestParam Integer providerScore,
                                @RequestParam(required = false) String comment,
                                Model model) {
        try {
            Rating rating = submissionService.submitRating(bookingId, userId, vehicleScore, providerScore, comment);
            model.addAttribute("rating", rating);
            return "ui/rate-success";
        } catch (InvalidScoreException | BookingNotFoundException | BookingNotCompletedException | DuplicateRatingException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("bookingId", bookingId);
            model.addAttribute("userId", userId);
            model.addAttribute("vehicleScore", vehicleScore);
            model.addAttribute("providerScore", providerScore);
            model.addAttribute("comment", comment);
            return "ui/rate";
        }
    }

    @GetMapping("/vehicle/{vehicleId}/ratings")
    public String vehicleRatings(@PathVariable Long vehicleId, Model model) {
        List<Rating> ratings = queryService.getVehicleRatings(vehicleId);
        double average = queryService.getAverageScore(vehicleId);
        model.addAttribute("vehicleId", vehicleId);
        model.addAttribute("ratings", ratings);
        model.addAttribute("average", average);
        model.addAttribute("count", ratings.size());
        return "ui/vehicle-ratings";
    }

    @GetMapping("/all")
    public String allRatings(Model model) {
        List<Rating> ratings = ratingRepository.findAll();
        double avgVehicle = ratings.stream()
                .mapToInt(r -> r.getReview().getVehicleScore())
                .average()
                .orElse(0.0);
        double avgProvider = ratings.stream()
                .mapToInt(r -> r.getReview().getProviderScore())
                .average()
                .orElse(0.0);
        model.addAttribute("ratings", ratings);
        model.addAttribute("total", ratings.size());
        model.addAttribute("avgVehicle", avgVehicle);
        model.addAttribute("avgProvider", avgProvider);
        return "ui/all-ratings";
    }

    @GetMapping("/lookup")
    public String lookup(@RequestParam(required = false) Long vehicleId,
                          @RequestParam(required = false) Long providerId,
                          Model model) {
        model.addAttribute("vehicleId", vehicleId);
        model.addAttribute("providerId", providerId);

        if (vehicleId != null) {
            List<Rating> vehicleRatings = queryService.getVehicleRatings(vehicleId);
            model.addAttribute("vehicleRatings", vehicleRatings);
            model.addAttribute("vehicleAverage", queryService.getAverageScore(vehicleId));
        }
        if (providerId != null) {
            List<Rating> providerRatings = queryService.getProviderRatings(providerId);
            model.addAttribute("providerRatings", providerRatings);
        }
        return "ui/lookup";
    }
}
