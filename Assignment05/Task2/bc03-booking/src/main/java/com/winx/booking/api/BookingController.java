package com.winx.booking.api;

import com.winx.booking.api.dto.BookingResponse;
import com.winx.booking.api.dto.CreateBookingRequest;
import com.winx.booking.api.dto.EndBookingRequest;
import com.winx.booking.domain.exception.BookingNotFoundException;
import com.winx.booking.domain.model.Booking;
import com.winx.booking.domain.model.BookingStatus;
import com.winx.booking.domain.repository.BookingRepository;
import com.winx.booking.domain.service.BookingEndResult;
import com.winx.booking.domain.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    public BookingController(BookingService bookingService, BookingRepository bookingRepository) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        Booking booking = bookingService.createBooking(request.getUserId(), request.getVehicleId());
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(booking));
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<BookingResponse> endBooking(@PathVariable Long id, @Valid @RequestBody EndBookingRequest request) {
        BookingEndResult result = bookingService.endBooking(id, request.getEndLatitude(), request.getEndLongitude(),
                request.paymentMethodOrDefault());
        return ResponseEntity.ok(BookingResponse.from(result.booking(), result.paymentOutcome()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        Booking booking = bookingService.cancelBooking(id);
        return ResponseEntity.ok(BookingResponse.from(booking));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
        return ResponseEntity.ok(BookingResponse.from(booking));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getUserBookings(@PathVariable Long userId) {
        List<BookingResponse> bookings = bookingRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(Booking::getStartTime).reversed())
                .map(BookingResponse::from)
                .toList();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<BookingResponse> getActiveBooking(@PathVariable Long userId) {
        return bookingRepository.findByUserIdAndStatus(userId, BookingStatus.ACTIVE)
                .map(booking -> ResponseEntity.ok(BookingResponse.from(booking)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Read-only, additive endpoint: all bookings ever made against a given
     * vehicle. Lets a Provider (via the Fleet context, which owns the
     * vehicleId) see the ride/earnings history for their own fleet, without
     * Booking exposing anything it doesn't already store per-booking.
     */
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<BookingResponse>> getVehicleBookings(@PathVariable Long vehicleId) {
        List<BookingResponse> bookings = bookingRepository.findByVehicleId(vehicleId).stream()
                .sorted(Comparator.comparing(Booking::getStartTime).reversed())
                .map(BookingResponse::from)
                .toList();
        return ResponseEntity.ok(bookings);
    }
}
