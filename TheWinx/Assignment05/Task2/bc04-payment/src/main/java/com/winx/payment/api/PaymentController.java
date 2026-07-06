package com.winx.payment.api;

import com.winx.payment.api.dto.CreatePaymentRequest;
import com.winx.payment.api.dto.PaymentResponse;
import com.winx.payment.domain.exception.PaymentNotFoundException;
import com.winx.payment.domain.model.Payment;
import com.winx.payment.domain.repository.PaymentRepository;
import com.winx.payment.domain.service.PaymentProcessingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentProcessingService paymentProcessingService;
    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentProcessingService paymentProcessingService, PaymentRepository paymentRepository) {
        this.paymentProcessingService = paymentProcessingService;
        this.paymentRepository = paymentRepository;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request) {
        Payment payment = paymentProcessingService.initiatePayment(
                request.bookingId(),
                request.amount(),
                request.currency(),
                request.userId(),
                request.paymentMethod(),
                request.maskedReferenceOrDefault());
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.from(payment));
    }

    @GetMapping("/{id}")
    public PaymentResponse getById(@PathVariable Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> PaymentNotFoundException.forId(id));
        return PaymentResponse.from(payment);
    }

    @GetMapping("/booking/{bookingId}")
    public PaymentResponse getByBookingId(@PathVariable Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> PaymentNotFoundException.forBookingId(bookingId));
        return PaymentResponse.from(payment);
    }

    @PostMapping("/{id}/retry")
    public PaymentResponse retry(@PathVariable Long id) {
        Payment payment = paymentProcessingService.retryPayment(id);
        return PaymentResponse.from(payment);
    }
}
