package com.winx.payment.infrastructure;

import com.winx.payment.domain.model.Money;
import com.winx.payment.domain.model.PaymentMethod;
import com.winx.payment.domain.model.PaymentResult;
import com.winx.payment.domain.model.PaymentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mock implementation of the external payment gateway. Deliberate test hook:
 * a masked reference containing "fail" (case-insensitive) simulates a
 * declined card so the demo/tests can trigger a FAILED result on demand.
 */
@Component
public class MockPaymentGatewayAdapter implements PaymentGatewayAdapter {

    @Override
    public PaymentResult charge(Money money, PaymentMethod method) {
        // A zero-amount charge (e.g. a ~0-distance per-km ride) is nothing
        // owed, not an error - it settles trivially as PAID. Only a genuinely
        // invalid (negative or missing) amount is a simulated failure here.
        if (money == null || money.getAmount() == null || money.getAmount().signum() < 0) {
            return new PaymentResult(PaymentStatus.FAILED, null, "Invalid amount (simulated)");
        }
        String maskedReference = method == null ? null : method.getMaskedReference();
        if (maskedReference != null && maskedReference.toLowerCase().contains("fail")) {
            return new PaymentResult(PaymentStatus.FAILED, null, "Card declined (simulated)");
        }
        return new PaymentResult(PaymentStatus.PAID, LocalDateTime.now(), null);
    }
}
