package com.winx.booking.api.ui;

import com.winx.booking.api.dto.BookingCreateRequest;
import com.winx.booking.api.dto.BookingDto;
import com.winx.booking.application.BookingService;
import com.winx.booking.domain.Booking;
import com.winx.booking.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Server-rendered Thymeleaf UI for BC-03 (standalone, demo-only). Reuses {@link BookingService};
 * the REST controller's {@code GlobalExceptionHandler} does not apply here, so domain errors are
 * caught and surfaced as flash messages.
 */
@Controller
@RequestMapping("/ui")
@RequiredArgsConstructor
public class BookingWebController {

    private final BookingService service;

    @GetMapping("/search")
    public String search(@RequestParam(required = false) Double lat,
                         @RequestParam(required = false) Double lon,
                         @RequestParam(required = false, defaultValue = "2.0") Double radiusKm,
                         @RequestParam(required = false) String type,
                         @RequestParam(required = false) String token,
                         Model model) {
        if (lat != null && lon != null) {
            model.addAttribute("results", service.searchVehicles(lat, lon, radiusKm, type, null));
        }
        model.addAttribute("lat", lat);
        model.addAttribute("lon", lon);
        model.addAttribute("radiusKm", radiusKm);
        model.addAttribute("type", type);
        model.addAttribute("token", token);
        return "search";
    }

    @PostMapping("/bookings/create")
    public String create(@RequestParam String token,
                         @RequestParam Long vehicleId,
                         @RequestParam Double startLatitude,
                         @RequestParam Double startLongitude,
                         RedirectAttributes ra) {
        try {
            Booking booking = service.createBooking(token,
                    new BookingCreateRequest(vehicleId, startLatitude, startLongitude));
            return "redirect:/ui/bookings?userId=" + booking.getUserId();
        } catch (DomainException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ui/search";
        }
    }

    @GetMapping("/bookings")
    public String bookings(@RequestParam(required = false) Long userId, Model model) {
        if (userId != null) {
            model.addAttribute("bookings",
                    service.findByUser(userId).stream().map(BookingDto::from).toList());
        }
        model.addAttribute("userId", userId);
        return "bookings";
    }

    @GetMapping("/bookings/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("booking", BookingDto.from(service.findById(id)));
        return "booking-detail";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.cancelBooking(id);
        } catch (DomainException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ui/bookings/" + id;
    }
}
