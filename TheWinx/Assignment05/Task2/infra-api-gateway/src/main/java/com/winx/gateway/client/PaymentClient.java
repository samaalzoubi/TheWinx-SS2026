package com.winx.gateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(name = "bc04-payment")
public interface PaymentClient {

    @GetMapping("/api/payments/booking/{bookingId}")
    PaymentResponse getByBooking(@PathVariable("bookingId") Long bookingId);

    record PaymentResponse(Long id, Long bookingId, Long userId, BigDecimal amount, String currency,
                            String paymentMethodType, String maskedReference, String status,
                            String paidAt, String failureReason) {
    }
}
