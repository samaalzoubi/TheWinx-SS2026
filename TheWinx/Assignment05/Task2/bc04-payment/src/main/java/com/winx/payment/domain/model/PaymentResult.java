package com.winx.payment.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;

@Embeddable
public class PaymentResult {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "failure_reason")
    private String failureReason;

    protected PaymentResult() {
        // JPA
    }

    public PaymentResult(PaymentStatus status, LocalDateTime paidAt, String failureReason) {
        this.status = status;
        this.paidAt = paidAt;
        this.failureReason = failureReason;
    }

    public static PaymentResult pending() {
        return new PaymentResult(PaymentStatus.PENDING, null, null);
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    void markPaid(LocalDateTime paidAt) {
        this.status = PaymentStatus.PAID;
        this.paidAt = paidAt;
        this.failureReason = null;
    }

    void markFailed(String failureReason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
        this.paidAt = null;
    }
}
