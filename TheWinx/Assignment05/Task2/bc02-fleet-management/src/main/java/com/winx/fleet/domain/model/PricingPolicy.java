package com.winx.fleet.domain.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;

@Embeddable
public class PricingPolicy {

    private BigDecimal pricePerUnit;

    @Enumerated(EnumType.STRING)
    private BillingModel billingModel;

    public PricingPolicy() {
    }

    public PricingPolicy(BigDecimal pricePerUnit, BillingModel billingModel) {
        this.pricePerUnit = pricePerUnit;
        this.billingModel = billingModel;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(BigDecimal pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public BillingModel getBillingModel() {
        return billingModel;
    }

    public void setBillingModel(BillingModel billingModel) {
        this.billingModel = billingModel;
    }
}
