package com.winx.gateway.web;

import com.winx.gateway.client.BookingClient;
import com.winx.gateway.client.RatingClient;
import feign.FeignException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RatingController {

    private final RatingClient ratingClient;
    private final BookingClient bookingClient;

    public RatingController(RatingClient ratingClient, BookingClient bookingClient) {
        this.ratingClient = ratingClient;
        this.bookingClient = bookingClient;
    }

    @GetMapping("/ratings/new")
    public String newRatingForm(HttpSession session, @RequestParam Long bookingId, Model model) {
        if (SessionUtil.requireRole(session, "USER") == null) {
            return "redirect:/login";
        }
        try {
            BookingClient.BookingResponse booking = bookingClient.getBooking(bookingId);
            model.addAttribute("booking", booking);
        } catch (FeignException e) {
            model.addAttribute("error", "Could not load that booking (HTTP " + e.status() + ").");
        }
        model.addAttribute("bookingId", bookingId);
        return "rate";
    }

    @PostMapping("/ratings")
    public String submit(HttpSession session, @RequestParam Long bookingId,
                          @RequestParam Integer vehicleScore, @RequestParam Integer providerScore,
                          @RequestParam(required = false) String comment) {
        Long userId = SessionUtil.requireRole(session, "USER");
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            ratingClient.submit(new RatingClient.SubmitRatingRequest(bookingId, userId, vehicleScore, providerScore, comment));
        } catch (FeignException ignored) {
            // we just let the dashboard keep showing "Rate this ride" if this fails, good enough for this demo's scope
        }
        return "redirect:/dashboard";
    }
}
