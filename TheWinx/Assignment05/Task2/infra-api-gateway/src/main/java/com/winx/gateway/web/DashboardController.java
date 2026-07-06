package com.winx.gateway.web;

import com.winx.gateway.client.BookingClient;
import com.winx.gateway.client.PaymentClient;
import com.winx.gateway.client.RatingClient;
import feign.FeignException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Comparator;
import java.util.List;

@Controller
public class DashboardController {

    private final BookingClient bookingClient;
    private final PaymentClient paymentClient;
    private final RatingClient ratingClient;

    public DashboardController(BookingClient bookingClient, PaymentClient paymentClient, RatingClient ratingClient) {
        this.bookingClient = bookingClient;
        this.paymentClient = paymentClient;
        this.ratingClient = ratingClient;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long userId = SessionUtil.requireRole(session, "USER");
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("userName", session.getAttribute("principalName"));

        List<BookingClient.BookingResponse> bookings;
        try {
            bookings = bookingClient.history(userId);
        } catch (FeignException e) {
            model.addAttribute("error", "Could not reach the Booking service (HTTP " + e.status() + "). Try again shortly.");
            bookings = List.of();
        }

        List<BookingRow> rows = bookings.stream()
                .sorted(Comparator.comparing(BookingClient.BookingResponse::startTime).reversed())
                .map(this::enrich)
                .toList();

        model.addAttribute("rows", rows);
        model.addAttribute("totalCount", rows.size());
        model.addAttribute("activeCount", rows.stream().filter(BookingRow::isActive).count());
        model.addAttribute("completedCount", rows.stream().filter(BookingRow::isCompleted).count());
        model.addAttribute("toRateCount", rows.stream().filter(BookingRow::canBeRated).count());
        return "dashboard";
    }

    /** Full detail for a single one of the current user's own bookings. */
    @GetMapping("/bookings/{id}")
    public String bookingDetail(HttpSession session, @PathVariable Long id, Model model) {
        Long userId = SessionUtil.requireRole(session, "USER");
        if (userId == null) {
            return "redirect:/login";
        }
        BookingClient.BookingResponse booking;
        try {
            booking = bookingClient.getBooking(id);
        } catch (FeignException.NotFound e) {
            model.addAttribute("error", "That booking doesn't exist.");
            return "booking-detail";
        } catch (FeignException e) {
            model.addAttribute("error", "Could not reach the Booking service (HTTP " + e.status() + ").");
            return "booking-detail";
        }
        if (!userId.equals(booking.userId())) {
            model.addAttribute("error", "That booking doesn't belong to you.");
            return "booking-detail";
        }
        model.addAttribute("row", enrich(booking));
        return "booking-detail";
    }

    private BookingRow enrich(BookingClient.BookingResponse booking) {
        PaymentClient.PaymentResponse payment = null;
        RatingClient.RatingResponse rating = null;

        if ("COMPLETED".equals(booking.status())) {
            try {
                payment = paymentClient.getByBooking(booking.id());
            } catch (FeignException.NotFound ignored) {
                // no payment recorded yet - fine, just show nothing for it
            } catch (FeignException e) {
                // Payment service unreachable: degrade gracefully, dashboard still renders
            }
            try {
                rating = ratingClient.getByBooking(booking.id());
            } catch (FeignException.NotFound ignored) {
                // not rated yet - the template offers a "Rate this ride" link
            } catch (FeignException e) {
                // Rating service unreachable: degrade gracefully
            }
        }

        return new BookingRow(booking, payment, rating);
    }
}
