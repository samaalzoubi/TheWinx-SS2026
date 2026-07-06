package com.winx.payment.domain.event;

import java.math.BigDecimal;

public record PaymentInitiated(Long paymentId, Long bookingId, BigDecimal amount) {
}
