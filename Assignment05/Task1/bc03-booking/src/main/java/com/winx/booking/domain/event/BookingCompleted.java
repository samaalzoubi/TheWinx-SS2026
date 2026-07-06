package com.winx.booking.domain.event;

import java.math.BigDecimal;

public record BookingCompleted(Long bookingId, BigDecimal totalCost) {
}
