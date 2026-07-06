package com.winx.fleet.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status;

    @Embedded
    private VehicleLocation location;

    @Embedded
    private PricingPolicy pricingPolicy;

    @Embedded
    private UsageRestrictions usageRestrictions;

    public Vehicle() {
    }

    public Vehicle(Long providerId, VehicleType type, String description, VehicleLocation location,
                   PricingPolicy pricingPolicy, UsageRestrictions usageRestrictions) {
        this.providerId = providerId;
        this.type = type;
        this.description = description;
        this.status = VehicleStatus.AVAILABLE;
        this.location = location;
        this.pricingPolicy = pricingPolicy;
        this.usageRestrictions = usageRestrictions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public VehicleLocation getLocation() {
        return location;
    }

    public void setLocation(VehicleLocation location) {
        this.location = location;
    }

    public PricingPolicy getPricingPolicy() {
        return pricingPolicy;
    }

    public void setPricingPolicy(PricingPolicy pricingPolicy) {
        this.pricingPolicy = pricingPolicy;
    }

    public UsageRestrictions getUsageRestrictions() {
        return usageRestrictions;
    }

    public void setUsageRestrictions(UsageRestrictions usageRestrictions) {
        this.usageRestrictions = usageRestrictions;
    }
}
