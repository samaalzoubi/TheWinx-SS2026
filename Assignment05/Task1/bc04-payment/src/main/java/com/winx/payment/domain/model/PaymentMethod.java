package com.winx.payment.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PaymentMethod {

    @Column(name = "payment_method_type", nullable = false)
    private String type;

    @Column(name = "masked_reference")
    private String maskedReference;

    protected PaymentMethod() {
        // we keep this no-arg only because JPA needs one to instantiate the entity via reflection
    }

    public PaymentMethod(String type, String maskedReference) {
        this.type = type;
        this.maskedReference = maskedReference;
    }

    public String getType() {
        return type;
    }

    public String getMaskedReference() {
        return maskedReference;
    }
}
