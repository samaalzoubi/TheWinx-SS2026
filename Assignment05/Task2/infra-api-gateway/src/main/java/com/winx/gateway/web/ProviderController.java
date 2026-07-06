package com.winx.gateway.web;

import com.winx.gateway.client.BookingClient;
import com.winx.gateway.client.FleetClient;
import com.winx.gateway.client.PaymentClient;
import com.winx.gateway.client.RatingClient;
import feign.FeignException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Everything a Provider needs, in one place: fleet overview (R15), vehicle
 * CRUD (R12), pricing (R13) and restrictions (R14) - all against the real
 * Fleet Management API - plus the ratings their fleet/company has received
 * and their earnings, both composed here in the portal from Booking/Payment/
 * Rating data (none of those bounded contexts know about "Provider" beyond
 * the plain providerId/vehicleId references they already store).
 */
@Controller
@RequestMapping("/provider")
public class ProviderController {

    private final FleetClient fleetClient;
    private final RatingClient ratingClient;
    private final BookingClient bookingClient;
    private final PaymentClient paymentClient;

    public ProviderController(FleetClient fleetClient, RatingClient ratingClient,
                               BookingClient bookingClient, PaymentClient paymentClient) {
        this.fleetClient = fleetClient;
        this.ratingClient = ratingClient;
        this.bookingClient = bookingClient;
        this.paymentClient = paymentClient;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long providerId = SessionUtil.requireRole(session, "PROVIDER");
        if (providerId == null) {
            return "redirect:/login";
        }
        model.addAttribute("companyName", session.getAttribute("principalName"));

        List<FleetClient.VehicleResponse> vehicles;
        try {
            vehicles = fleetClient.listByProvider(providerId);
        } catch (FeignException e) {
            model.addAttribute("error", "Could not reach the Fleet Management service (HTTP " + e.status() + ").");
            vehicles = List.of();
        }

        Map<Long, RatingClient.AverageResponse> averagesByVehicle = vehicles.stream()
                .collect(Collectors.toMap(FleetClient.VehicleResponse::id, v -> {
                    try {
                        return ratingClient.getVehicleAverage(v.id());
                    } catch (FeignException e) {
                        return new RatingClient.AverageResponse(v.id(), null, 0);
                    }
                }));

        int overallRatingCount = 0;
        try {
            overallRatingCount = ratingClient.getByProvider(providerId).size();
        } catch (FeignException ignored) {
            // Rating service unreachable: just omit the count, fleet data still renders
        }

        long availableCount = vehicles.stream().filter(v -> "AVAILABLE".equals(v.status())).count();
        long bookedCount = vehicles.stream().filter(v -> "BOOKED".equals(v.status())).count();

        model.addAttribute("vehicles", vehicles);
        model.addAttribute("averages", averagesByVehicle);
        model.addAttribute("overallRatingCount", overallRatingCount);
        model.addAttribute("totalVehicles", vehicles.size());
        model.addAttribute("availableCount", availableCount);
        model.addAttribute("bookedCount", bookedCount);
        return "provider-dashboard";
    }

    /** Full list of ratings for this provider's fleet - the count was already shown on the dashboard, this is the detail behind it. */
    @GetMapping("/ratings")
    public String ratings(HttpSession session, Model model) {
        Long providerId = SessionUtil.requireRole(session, "PROVIDER");
        if (providerId == null) {
            return "redirect:/login";
        }
        model.addAttribute("companyName", session.getAttribute("principalName"));
        try {
            List<RatingClient.RatingResponse> ratings = ratingClient.getByProvider(providerId).stream()
                    .sorted(Comparator.comparing(RatingClient.RatingResponse::createdAt).reversed())
                    .toList();
            model.addAttribute("ratings", ratings);
        } catch (FeignException e) {
            model.addAttribute("error", "Could not reach the Rating service (HTTP " + e.status() + ").");
            model.addAttribute("ratings", List.of());
        }
        return "provider-ratings";
    }

    /**
     * Earnings composed live from three services: this provider's vehicles
     * (Fleet), the bookings made against each vehicle (Booking, via the new
     * read-only /api/bookings/vehicle/{id} endpoint), and the payment for
     * each completed one (Payment, via the same read the rider dashboard
     * and bc03's own booking detail page already use). Each hop degrades
     * gracefully on its own if a service is down.
     */
    @GetMapping("/earnings")
    public String earnings(HttpSession session, Model model) {
        Long providerId = SessionUtil.requireRole(session, "PROVIDER");
        if (providerId == null) {
            return "redirect:/login";
        }
        model.addAttribute("companyName", session.getAttribute("principalName"));

        List<FleetClient.VehicleResponse> vehicles;
        try {
            vehicles = fleetClient.listByProvider(providerId);
        } catch (FeignException e) {
            model.addAttribute("error", "Could not reach the Fleet Management service (HTTP " + e.status() + ").");
            vehicles = List.of();
        }

        List<EarningsRow> rows = new ArrayList<>();
        for (FleetClient.VehicleResponse vehicle : vehicles) {
            List<BookingClient.BookingResponse> bookings;
            try {
                bookings = bookingClient.byVehicle(vehicle.id());
            } catch (FeignException e) {
                continue; // Booking unreachable for this vehicle - skip, don't fail the whole page
            }
            for (BookingClient.BookingResponse booking : bookings) {
                if (!"COMPLETED".equals(booking.status())) {
                    continue;
                }
                PaymentClient.PaymentResponse payment = null;
                try {
                    payment = paymentClient.getByBooking(booking.id());
                } catch (FeignException ignored) {
                    // Payment unreachable or not recorded - row still shows the ride, payment column is "unknown"
                }
                rows.add(new EarningsRow(vehicle, booking, payment));
            }
        }
        rows.sort(Comparator.comparing((EarningsRow r) -> r.booking().startTime()).reversed());

        BigDecimal totalRevenue = rows.stream()
                .filter(r -> r.payment() != null && "PAID".equals(r.payment().status()))
                .map(r -> r.payment().amount())
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("rows", rows);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("rideCount", rows.size());
        return "provider-earnings";
    }

    public record EarningsRow(FleetClient.VehicleResponse vehicle, BookingClient.BookingResponse booking,
                               PaymentClient.PaymentResponse payment) {
    }

    @GetMapping("/vehicles/new")
    public String newVehicleForm(HttpSession session) {
        if (SessionUtil.requireRole(session, "PROVIDER") == null) {
            return "redirect:/login";
        }
        return "provider-vehicle-form";
    }

    @PostMapping("/vehicles")
    public String createVehicle(HttpSession session,
                                 @RequestParam String type, @RequestParam String description,
                                 @RequestParam BigDecimal pricePerUnit, @RequestParam String billingModel,
                                 @RequestParam(required = false) Integer maxDurationMinutes,
                                 @RequestParam(required = false) Integer maxKilometers,
                                 @RequestParam(required = false) Integer minAge,
                                 @RequestParam(required = false) Integer maxPersons,
                                 @RequestParam double latitude, @RequestParam double longitude,
                                 Model model) {
        Long providerId = SessionUtil.requireRole(session, "PROVIDER");
        if (providerId == null) {
            return "redirect:/login";
        }
        try {
            fleetClient.create(new FleetClient.CreateVehicleRequest(providerId, type, description, pricePerUnit,
                    billingModel, maxDurationMinutes, maxKilometers, minAge, maxPersons, latitude, longitude));
            return "redirect:/provider/dashboard";
        } catch (FeignException.BadRequest e) {
            model.addAttribute("error", "Please check the values (e.g. price must be greater than 0).");
            return "provider-vehicle-form";
        } catch (FeignException e) {
            model.addAttribute("error", "Could not create the vehicle (HTTP " + e.status() + ").");
            return "provider-vehicle-form";
        }
    }

    @GetMapping("/vehicles/{id}/edit")
    public String editVehicleForm(HttpSession session, @PathVariable Long id, Model model) {
        if (SessionUtil.requireRole(session, "PROVIDER") == null) {
            return "redirect:/login";
        }
        try {
            model.addAttribute("vehicle", fleetClient.getVehicle(id));
        } catch (FeignException e) {
            model.addAttribute("error", "Could not load that vehicle (HTTP " + e.status() + ").");
        }
        return "provider-vehicle-edit";
    }

    @PostMapping("/vehicles/{id}/edit")
    public String updateVehicle(HttpSession session, @PathVariable Long id,
                                 @RequestParam String description, @RequestParam BigDecimal pricePerUnit,
                                 @RequestParam String billingModel,
                                 @RequestParam(required = false) Integer maxDurationMinutes,
                                 @RequestParam(required = false) Integer maxKilometers,
                                 @RequestParam(required = false) Integer minAge,
                                 @RequestParam(required = false) Integer maxPersons) {
        if (SessionUtil.requireRole(session, "PROVIDER") == null) {
            return "redirect:/login";
        }
        try {
            fleetClient.update(id, new FleetClient.UpdateVehicleRequest(description, pricePerUnit, billingModel,
                    maxDurationMinutes, maxKilometers, minAge, maxPersons));
        } catch (FeignException ignored) {
            // dashboard re-render is enough feedback for this demo scope
        }
        return "redirect:/provider/dashboard";
    }

    @PostMapping("/vehicles/{id}/delete")
    public String deleteVehicle(HttpSession session, @PathVariable Long id) {
        if (SessionUtil.requireRole(session, "PROVIDER") == null) {
            return "redirect:/login";
        }
        try {
            fleetClient.delete(id);
        } catch (FeignException.Conflict e) {
            return "redirect:/provider/dashboard?error=" + java.net.URLEncoder.encode(
                    "Can't delete a vehicle that's currently booked.", java.nio.charset.StandardCharsets.UTF_8);
        } catch (FeignException ignored) {
        }
        return "redirect:/provider/dashboard";
    }
}
