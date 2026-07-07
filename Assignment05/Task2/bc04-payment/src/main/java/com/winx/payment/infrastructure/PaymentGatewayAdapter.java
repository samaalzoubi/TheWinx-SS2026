package com.winx.payment.infrastructure;

import com.winx.payment.domain.model.Money;
import com.winx.payment.domain.model.PaymentMethod;
import com.winx.payment.domain.model.PaymentResult;

// we have no real gateway integration, MockPaymentGatewayAdapter simulates the charge synchronously so we can hit both PAID and FAILED deterministically
public interface PaymentGatewayAdapter {

    PaymentResult charge(Money money, PaymentMethod method);
}
