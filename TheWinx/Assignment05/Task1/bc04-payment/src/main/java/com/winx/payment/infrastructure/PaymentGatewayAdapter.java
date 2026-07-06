package com.winx.payment.infrastructure;

import com.winx.payment.domain.model.Money;
import com.winx.payment.domain.model.PaymentMethod;
import com.winx.payment.domain.model.PaymentResult;

/**
 * Outbound port towards an external payment gateway. This bounded context
 * has no real integration - {@link MockPaymentGatewayAdapter} simulates the
 * charge synchronously so the demo/tests can exercise both PAID and FAILED
 * outcomes deterministically.
 */
public interface PaymentGatewayAdapter {

    PaymentResult charge(Money money, PaymentMethod method);
}
