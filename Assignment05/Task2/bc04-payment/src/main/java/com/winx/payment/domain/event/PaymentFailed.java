package com.winx.payment.domain.event;

public record PaymentFailed(Long paymentId, String reason) {
}
