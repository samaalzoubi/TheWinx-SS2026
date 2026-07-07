package com.winx.booking.domain.model;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.Objects;

// we snapshot the vehicle at booking time so a later price change never retroactively affects an in-progress or past ride
@Embeddable
public class VehicleSnapshot {

    private Long vehicleId;
    private String type;
    private BigDecimal pricePerUnit;
    private String billingModel;

    protected VehicleSnapshot() {
        // JPA
    }

    public VehicleSnapshot(Long vehicleId, String type, BigDecimal pricePerUnit, String billingModel) {
        this.vehicleId = vehicleId;
        this.type = type;
        this.pricePerUnit = pricePerUnit;
        this.billingModel = billingModel;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public String getBillingModel() {
        return billingModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehicleSnapshot that)) return false;
        return Objects.equals(vehicleId, that.vehicleId)
                && Objects.equals(type, that.type)
                && Objects.equals(pricePerUnit, that.pricePerUnit)
                && Objects.equals(billingModel, that.billingModel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleId, type, pricePerUnit, billingModel);
    }
}
