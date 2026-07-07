package com.winx.booking.infrastructure;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "bc04-payment")
public interface PaymentFeignClient {

    @PostMapping("/api/payments")
    PaymentFeignResponse charge(@RequestBody PaymentFeignRequest request);

    // we reuse this same already-public bc04 endpoint the gateway's own PaymentClient calls too
    @GetMapping("/api/payments/booking/{bookingId}")
    PaymentFeignResponse getByBooking(@PathVariable("bookingId") Long bookingId);

    record PaymentFeignRequest(
            Long bookingId,
            Long userId,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            String maskedReference) {
    }

    record PaymentFeignResponse(
            Long id,
            Long bookingId,
            Long userId,
            BigDecimal amount,
            String currency,
            String paymentMethodType,
            String maskedReference,
            String status,
            String paidAt,
            String failureReason) {
    }
}
