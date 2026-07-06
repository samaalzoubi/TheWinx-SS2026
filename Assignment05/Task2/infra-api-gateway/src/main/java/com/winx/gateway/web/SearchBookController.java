package com.winx.gateway.web;

import com.winx.gateway.client.BookingClient;
import com.winx.gateway.client.FleetClient;
import feign.FeignException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SearchBookController {

    private final FleetClient fleetClient;
    private final BookingClient bookingClient;

    public SearchBookController(FleetClient fleetClient, BookingClient bookingClient) {
        this.fleetClient = fleetClient;
        this.bookingClient = bookingClient;
    }

    @GetMapping("/search")
    public String search(HttpSession session,
                          @RequestParam(required = false) Double lat,
                          @RequestParam(required = false) Double lon,
                          @RequestParam(required = false, defaultValue = "5") double radiusKm,
                          @RequestParam(required = false) String type,
                          @RequestParam(required = false) Double maxPrice,
                          Model model) {
        if (SessionUtil.requireRole(session, "USER") == null) {
            return "redirect:/login";
        }
        // Default to Dortmund city centre so the form has sensible pre-filled values for the demo
        double searchLat = lat != null ? lat : 51.5136;
        double searchLon = lon != null ? lon : 7.4653;
        model.addAttribute("lat", searchLat);
        model.addAttribute("lon", searchLon);
        model.addAttribute("radiusKm", radiusKm);
        model.addAttribute("type", type);
        model.addAttribute("maxPrice", maxPrice);

        if (lat != null) {
            try {
                List<FleetClient.VehicleResponse> results = fleetClient.search(searchLat, searchLon, radiusKm,
                        (type == null || type.isBlank()) ? null : type, maxPrice);
                model.addAttribute("results", results);
            } catch (FeignException e) {
                model.addAttribute("error", "Could not reach the Fleet Management service (HTTP " + e.status() + ").");
            }
        }
        return "search";
    }

    @PostMapping("/bookings")
    public String book(HttpSession session, @RequestParam Long vehicleId, Model model) {
        Long userId = SessionUtil.requireRole(session, "USER");
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            bookingClient.create(new BookingClient.CreateBookingRequest(userId, vehicleId));
            return "redirect:/dashboard";
        } catch (FeignException.Conflict e) {
            return "redirect:/search?error=" + urlEncode("That vehicle is no longer available, or you already have an active ride.");
        } catch (FeignException.BadRequest e) {
            return "redirect:/search?error=" + urlEncode("You don't meet this vehicle's usage restrictions (e.g. minimum age).");
        } catch (FeignException e) {
            return "redirect:/search?error=" + urlEncode("Booking failed (HTTP " + e.status() + ").");
        }
    }

    @PostMapping("/bookings/{id}/end")
    public String endRide(HttpSession session, @PathVariable Long id,
                           @RequestParam Double endLatitude, @RequestParam Double endLongitude,
                           @RequestParam(defaultValue = "CARD") String paymentMethod) {
        if (SessionUtil.requireRole(session, "USER") == null) {
            return "redirect:/login";
        }
        try {
            bookingClient.end(id, new BookingClient.EndBookingRequest(endLatitude, endLongitude, paymentMethod));
        } catch (FeignException ignored) {
            // Surfacing this on the dashboard is enough; the booking simply stays ACTIVE if it failed.
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancel(HttpSession session, @PathVariable Long id) {
        if (SessionUtil.requireRole(session, "USER") == null) {
            return "redirect:/login";
        }
        try {
            bookingClient.cancel(id);
        } catch (FeignException ignored) {
        }
        return "redirect:/dashboard";
    }

    private static String urlEncode(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }
}
