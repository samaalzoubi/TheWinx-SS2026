package com.winx.booking.api.dto;

import jakarta.validation.constraints.NotNull;

public class EndBookingRequest {

    @NotNull
    private Double endLatitude;

    @NotNull
    private Double endLongitude;

    // we default this to CARD when omitted so existing callers keep working unchanged
    private String paymentMethod;

    public EndBookingRequest() {
    }

    public EndBookingRequest(Double endLatitude, Double endLongitude) {
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
    }

    public EndBookingRequest(Double endLatitude, Double endLongitude, String paymentMethod) {
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
        this.paymentMethod = paymentMethod;
    }

    public Double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(Double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public Double getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(Double endLongitude) {
        this.endLongitude = endLongitude;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String paymentMethodOrDefault() {
        return (paymentMethod == null || paymentMethod.isBlank()) ? "CARD" : paymentMethod;
    }
}
