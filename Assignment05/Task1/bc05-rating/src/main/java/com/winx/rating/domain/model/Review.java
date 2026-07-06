package com.winx.rating.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * The actual feedback content: numeric scores (1-5) for the vehicle and its
 * provider, plus an optional free-text comment. Score range validation is
 * enforced in the service layer so a domain-specific exception (with the
 * correct HTTP status) can be raised.
 */
@Embeddable
public class Review {

    @Column(name = "vehicle_score", nullable = false)
    private Integer vehicleScore;

    @Column(name = "provider_score", nullable = false)
    private Integer providerScore;

    @Column(name = "comment")
    private String comment;

    protected Review() {
        // JPA
    }

    public Review(Integer vehicleScore, Integer providerScore, String comment) {
        this.vehicleScore = vehicleScore;
        this.providerScore = providerScore;
        this.comment = comment;
    }

    public Integer getVehicleScore() {
        return vehicleScore;
    }

    public Integer getProviderScore() {
        return providerScore;
    }

    public String getComment() {
        return comment;
    }
}
