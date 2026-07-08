package com.winx.rating.domain.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public class Review {

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "vehicle_score", nullable = false))
    private Score vehicleScore;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "provider_score", nullable = false))
    private Score providerScore;

    @Column(name = "comment")
    private String comment;

    protected Review() {
    }

    public Review(Score vehicleScore, Score providerScore, String comment) {
        this.vehicleScore = vehicleScore;
        this.providerScore = providerScore;
        this.comment = comment;
    }

    public Score getVehicleScore() {
        return vehicleScore;
    }

    public Score getProviderScore() {
        return providerScore;
    }

    public String getComment() {
        return comment;
    }
}
