package com.winx.booking.domain.event;

import java.math.BigDecimal;

public record PaymentTriggered(Long bookingId, BigDecimal amount, Long userId) {
}
