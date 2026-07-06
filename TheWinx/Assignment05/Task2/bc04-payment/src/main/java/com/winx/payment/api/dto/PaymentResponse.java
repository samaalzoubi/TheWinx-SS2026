package com.winx.payment.api.dto;

import com.winx.payment.domain.model.Payment;
import com.winx.payment.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long bookingId,
        Long userId,
        BigDecimal amount,
        String currency,
        String paymentMethodType,
        String maskedReference,
        PaymentStatus status,
        LocalDateTime paidAt,
        String failureReason
) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBookingId(),
                payment.getUserId(),
                payment.getMoney().getAmount(),
                payment.getMoney().getCurrency(),
                payment.getPaymentMethod().getType(),
                payment.getPaymentMethod().getMaskedReference(),
                payment.getResult().getStatus(),
                payment.getResult().getPaidAt(),
                payment.getResult().getFailureReason()
        );
    }
}
