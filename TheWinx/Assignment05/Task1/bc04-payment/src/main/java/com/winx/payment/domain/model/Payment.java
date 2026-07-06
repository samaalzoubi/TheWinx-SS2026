package com.winx.payment.domain.model;

import com.winx.payment.domain.exception.InvalidPaymentStateException;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Aggregate root for the Payment bounded context. Represents a single
 * financial transaction tied to a completed booking. bookingId/userId are
 * reference-only identifiers; this context owns no booking logic.
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookingId;

    private Long userId;

    @Embedded
    private Money money;

    @Embedded
    private PaymentMethod paymentMethod;

    @Embedded
    private PaymentResult result;

    protected Payment() {
        // JPA
    }

    public Payment(Long bookingId, Long userId, Money money, PaymentMethod paymentMethod) {
        // Money's invariant is non-negative, not strictly positive - a ride
        // with ~0 measured distance under per-km billing legitimately costs
        // 0 and is still a real payment to record, not an invalid one.
        if (money == null || money.getAmount() == null || money.getAmount().signum() < 0) {
            throw new IllegalArgumentException("Payment amount must not be negative");
        }
        this.bookingId = bookingId;
        this.userId = userId;
        this.money = money;
        this.paymentMethod = paymentMethod;
        this.result = PaymentResult.pending();
    }

    public void markPaid(LocalDateTime paidAt) {
        requireMutable();
        if (result.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Payment " + id + " cannot transition to PAID from " + result.getStatus());
        }
        this.result.markPaid(paidAt);
    }

    public void markFailed(String reason) {
        requireMutable();
        if (result.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Payment " + id + " cannot transition to FAILED from " + result.getStatus());
        }
        this.result.markFailed(reason);
    }

    /** Used when a FAILED payment is retried and the gateway is re-invoked. */
    public void resetForRetry() {
        if (result.getStatus() != PaymentStatus.FAILED) {
            throw new InvalidPaymentStateException(
                    "Payment " + id + " can only be retried when currently FAILED (was " + result.getStatus() + ")");
        }
        this.result = PaymentResult.pending();
    }

    private void requireMutable() {
        if (result.getStatus() == PaymentStatus.PAID) {
            throw new InvalidPaymentStateException("Payment " + id + " is PAID and can no longer be modified");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getUserId() {
        return userId;
    }

    public Money getMoney() {
        return money;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentResult getResult() {
        return result;
    }
}
