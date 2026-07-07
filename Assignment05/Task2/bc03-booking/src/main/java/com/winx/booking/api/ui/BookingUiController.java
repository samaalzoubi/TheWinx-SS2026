package com.winx.booking.api.ui;

import com.winx.booking.domain.model.Booking;
import com.winx.booking.domain.model.BookingStatus;
import com.winx.booking.domain.repository.BookingRepository;
import com.winx.booking.domain.service.BookingService;
import com.winx.booking.domain.service.VehicleSearchService;
import com.winx.booking.infrastructure.PaymentClient;
import com.winx.booking.infrastructure.PaymentView;
import com.winx.booking.infrastructure.VehicleView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ui")
public class BookingUiController {

    private final VehicleSearchService vehicleSearchService;
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final PaymentClient paymentClient;

    public BookingUiController(VehicleSearchService vehicleSearchService,
                                BookingService bookingService,
                                BookingRepository bookingRepository,
                                PaymentClient paymentClient) {
        this.vehicleSearchService = vehicleSearchService;
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.paymentClient = paymentClient;
    }

    @GetMapping
    public String home(Model model) {
        List<Booking> all = bookingRepository.findAll();
        model.addAttribute("totalCount", all.size());
        model.addAttribute("activeCount", all.stream().filter(b -> b.getStatus() == BookingStatus.ACTIVE).count());
        model.addAttribute("completedCount", all.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count());
        model.addAttribute("cancelledCount", all.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count());
        return "home";
    }

    @GetMapping("/my-bookings")
    public String myBookingsLookup(@RequestParam(required = false) Long userId) {
        return "redirect:/ui/my-bookings/" + (userId != null ? userId : 1L);
    }

    @GetMapping("/search-book")
    public String searchForm(@RequestParam(required = false) Long userId,
                              @RequestParam(required = false) String location,
                              @RequestParam(required = false) Double radiusKm,
                              @RequestParam(required = false) String type,
                              @RequestParam(required = false) Double maxPrice,
                              Model model) {
        double resolvedRadius = radiusKm != null ? radiusKm : 10.0;
        String resolvedType = (type != null && !type.isBlank()) ? type : null;

        model.addAttribute("userId", userId);
        model.addAttribute("locations", DemoLocations.all());
        model.addAttribute("selectedLocation", location);
        model.addAttribute("radiusKm", resolvedRadius);
        model.addAttribute("type", type);
        model.addAttribute("maxPrice", maxPrice);

        DemoLocations.Location loc = DemoLocations.find(location);
        if (loc != null) {
            List<VehicleView> vehicles = vehicleSearchService.findAvailableNear(
                    loc.latitude(), loc.longitude(), resolvedRadius, resolvedType, maxPrice);
            model.addAttribute("vehicles", vehicles);
        }
        return "search-book";
    }

    @PostMapping("/search-book/book")
    public String book(@RequestParam Long userId, @RequestParam Long vehicleId, RedirectAttributes redirectAttributes) {
        try {
            Booking booking = bookingService.createBooking(userId, vehicleId);
            redirectAttributes.addFlashAttribute("message", "Booking #" + booking.getId() + " created.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/ui/my-bookings/" + userId;
    }

    @GetMapping("/my-bookings/{userId}")
    public String myBookings(@PathVariable Long userId, Model model) {
        List<Booking> bookings = bookingRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(Booking::getStartTime).reversed())
                .toList();
        model.addAttribute("userId", userId);
        model.addAttribute("bookings", bookings);
        return "my-bookings";
    }

    @PostMapping("/my-bookings/{userId}/{bookingId}/end")
    public String endBooking(@PathVariable Long userId, @PathVariable Long bookingId,
                              @RequestParam Double endLatitude, @RequestParam Double endLongitude,
                              @RequestParam(defaultValue = "CARD") String paymentMethod,
                              RedirectAttributes redirectAttributes) {
        try {
            bookingService.endBooking(bookingId, endLatitude, endLongitude, paymentMethod);
            redirectAttributes.addFlashAttribute("message", "Booking #" + bookingId + " completed.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/ui/my-bookings/" + userId;
    }

    @PostMapping("/my-bookings/{userId}/{bookingId}/cancel")
    public String cancelBooking(@PathVariable Long userId, @PathVariable Long bookingId,
                                 RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(bookingId);
            redirectAttributes.addFlashAttribute("message", "Booking #" + bookingId + " cancelled.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/ui/my-bookings/" + userId;
    }

    @GetMapping("/all")
    public String allBookings(@RequestParam(required = false) String status, Model model) {
        List<Booking> all = bookingRepository.findAll().stream()
                .sorted(Comparator.comparing(Booking::getStartTime).reversed())
                .toList();

        long activeCount = all.stream().filter(b -> b.getStatus() == BookingStatus.ACTIVE).count();
        long completedCount = all.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count();
        long cancelledCount = all.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();
        BigDecimal totalRevenue = all.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .map(b -> b.getRideSummary().getTotalCost())
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String filter = (status == null || status.isBlank()) ? "ALL" : status.toUpperCase();
        List<Booking> filtered = "ALL".equals(filter)
                ? all
                : all.stream().filter(b -> b.getStatus().name().equals(filter)).toList();

        // we reuse the same resilient PaymentClient the booking service charges through, so a down bc04 just shows "unknown" here instead of breaking the page
        Map<Long, PaymentView> paymentsByBooking = new HashMap<>();
        for (Booking b : filtered) {
            if (b.getStatus() == BookingStatus.COMPLETED) {
                paymentsByBooking.put(b.getId(), paymentClient.findByBooking(b.getId()));
            }
        }

        model.addAttribute("bookings", filtered);
        model.addAttribute("payments", paymentsByBooking);
        model.addAttribute("statusFilter", filter);
        model.addAttribute("totalCount", all.size());
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("totalRevenue", totalRevenue);
        return "all-bookings";
    }

    @GetMapping("/bookings/{id}")
    public String bookingDetail(@PathVariable Long id, Model model) {
        bookingRepository.findById(id).ifPresentOrElse(
                booking -> {
                    model.addAttribute("booking", booking);
                    if (booking.getStatus() == BookingStatus.COMPLETED) {
                        model.addAttribute("payment", paymentClient.findByBooking(id));
                    }
                },
                () -> model.addAttribute("bookingId", id));
        return "booking-detail";
    }
}
