package com.winx.booking.api.dto;

import jakarta.validation.constraints.NotNull;

public class CreateBookingRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long vehicleId;

    public CreateBookingRequest() {
    }

    public CreateBookingRequest(Long userId, Long vehicleId) {
        this.userId = userId;
        this.vehicleId = vehicleId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }
}
