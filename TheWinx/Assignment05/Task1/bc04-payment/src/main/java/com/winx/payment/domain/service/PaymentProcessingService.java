package com.winx.payment.domain.service;

import com.winx.payment.domain.event.PaymentFailed;
import com.winx.payment.domain.event.PaymentInitiated;
import com.winx.payment.domain.event.PaymentSucceeded;
import com.winx.payment.domain.exception.InvalidPaymentStateException;
import com.winx.payment.domain.exception.PaymentNotFoundException;
import com.winx.payment.domain.model.Money;
import com.winx.payment.domain.model.Payment;
import com.winx.payment.domain.model.PaymentMethod;
import com.winx.payment.domain.model.PaymentResult;
import com.winx.payment.domain.model.PaymentStatus;
import com.winx.payment.domain.repository.PaymentRepository;
import com.winx.payment.infrastructure.PaymentGatewayAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Domain service orchestrating payment processing against the (mock)
 * external payment gateway. Owns the invariant that amounts must be
 * positive and that status transitions only ever move PENDING -> PAID or
 * PENDING -> FAILED, never backwards.
 */
@Service
public class PaymentProcessingService {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessingService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayAdapter paymentGatewayAdapter;

    public PaymentProcessingService(PaymentRepository paymentRepository, PaymentGatewayAdapter paymentGatewayAdapter) {
        this.paymentRepository = paymentRepository;
        this.paymentGatewayAdapter = paymentGatewayAdapter;
    }

    @Transactional
    public Payment initiatePayment(Long bookingId, BigDecimal amount, String currency, Long userId,
                                    String paymentMethodType, String paymentMethodMaskedRef) {
        // Money's invariant is non-negative, not strictly positive - a ride
        // that measured ~0 distance under per-km billing legitimately costs
        // 0 and must still be recorded as a real (PAID) payment, not rejected.
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Payment amount must not be negative");
        }

        Money money = new Money(amount, currency);
        PaymentMethod method = new PaymentMethod(paymentMethodType, paymentMethodMaskedRef);
        Payment payment = new Payment(bookingId, userId, money, method);
        payment = paymentRepository.save(payment);

        log.info("Domain event: {}", new PaymentInitiated(payment.getId(), payment.getBookingId(), amount));

        settle(payment, money, method);

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment retryPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> PaymentNotFoundException.forId(paymentId));

        if (payment.getResult().getStatus() != PaymentStatus.FAILED) {
            throw new InvalidPaymentStateException(
                    "Payment " + paymentId + " can only be retried when currently FAILED (was "
                            + payment.getResult().getStatus() + ")");
        }

        payment.resetForRetry();
        settle(payment, payment.getMoney(), payment.getPaymentMethod());

        return paymentRepository.save(payment);
    }

    private void settle(Payment payment, Money money, PaymentMethod method) {
        PaymentResult result = paymentGatewayAdapter.charge(money, method);
        if (result.getStatus() == PaymentStatus.PAID) {
            payment.markPaid(result.getPaidAt());
            log.info("Domain event: {}", new PaymentSucceeded(payment.getId(), payment.getResult().getPaidAt()));
        } else {
            payment.markFailed(result.getFailureReason());
            log.info("Domain event: {}", new PaymentFailed(payment.getId(), payment.getResult().getFailureReason()));
        }
    }
}
