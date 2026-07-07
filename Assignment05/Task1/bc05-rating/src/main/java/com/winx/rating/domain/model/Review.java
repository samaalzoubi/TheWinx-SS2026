package com.winx.rating.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// we don't validate the score range here - that happens in the service layer so we can raise a proper domain exception with the right HTTP status
@Embeddable
public class Review {

    @Column(name = "vehicle_score", nullable = false)
    private Integer vehicleScore;

    @Column(name = "provider_score", nullable = false)
    private Integer providerScore;

    @Column(name = "comment")
    private String comment;

    protected Review() {
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
