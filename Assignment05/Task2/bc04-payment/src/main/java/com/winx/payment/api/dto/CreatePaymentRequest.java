package com.winx.payment.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotNull Long bookingId,
        @NotNull Long userId,
        // we used @PositiveOrZero here since a ~0km ride under per-km billing is a legit zero-cost charge, matching Money's non-negative invariant
        @NotNull @PositiveOrZero BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String paymentMethod,
        String maskedReference
) {

    public String maskedReferenceOrDefault() {
        return (maskedReference == null || maskedReference.isBlank()) ? "****" : maskedReference;
    }
}
