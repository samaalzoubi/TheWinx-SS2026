package com.winx.booking.infrastructure;

import java.math.BigDecimal;

// we don't distinguish "no payment yet" from "Payment unreachable" here, null covers both since the UI treats them the same
public record PaymentView(Long paymentId, String status, String failureReason, BigDecimal amount, String paidAt) {
}
