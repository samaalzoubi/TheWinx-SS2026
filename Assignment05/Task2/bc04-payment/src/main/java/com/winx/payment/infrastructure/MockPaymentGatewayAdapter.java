package com.winx.payment.infrastructure;

import com.winx.payment.domain.model.Money;
import com.winx.payment.domain.model.PaymentMethod;
import com.winx.payment.domain.model.PaymentResult;
import com.winx.payment.domain.model.PaymentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// we added a test hook here: a masked reference containing "fail" (case-insensitive) simulates a declined card on demand
@Component
public class MockPaymentGatewayAdapter implements PaymentGatewayAdapter {

    @Override
    public PaymentResult charge(Money money, PaymentMethod method) {
        // a zero-amount charge is nothing owed, not an error, so it settles as PAID, only a negative/missing amount fails here
        if (money == null || money.getAmount() == null || money.getAmount().signum() < 0) {
            return new PaymentResult(PaymentStatus.FAILED, null, "Invalid amount (simulated)");
        }
        String maskedReference = method == null ? null : method.getMaskedReference();
        if (maskedReference != null && maskedReference.toLowerCase().contains("gateway-down")) {
            // second test hook: this simulates the gateway being unreachable (a technical failure) so we can demo the circuit breaker
            throw new PaymentGatewayUnavailableException("Simulated gateway timeout");
        }
        if (maskedReference != null && maskedReference.toLowerCase().contains("fail")) {
            return new PaymentResult(PaymentStatus.FAILED, null, "Card declined (simulated)");
        }
        return new PaymentResult(PaymentStatus.PAID, LocalDateTime.now(), null);
    }
}
