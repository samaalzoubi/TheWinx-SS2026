package com.winx.payment.domain.event;

import java.time.LocalDateTime;

public record PaymentSucceeded(Long paymentId, LocalDateTime paidAt) {
}
