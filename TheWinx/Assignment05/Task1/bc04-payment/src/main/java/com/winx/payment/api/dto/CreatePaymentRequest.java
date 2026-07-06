package com.winx.payment.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotNull Long bookingId,
        @NotNull Long userId,
        // Zero is a valid charge (e.g. a ride with ~0 measured distance under
        // per-km billing) - only a negative amount is ever invalid. Matches
        // the Money value object's documented "non-negative" invariant.
        @NotNull @PositiveOrZero BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String paymentMethod,
        String maskedReference
) {

    public String maskedReferenceOrDefault() {
        return (maskedReference == null || maskedReference.isBlank()) ? "****" : maskedReference;
    }
}
