package com.winx.booking.api.dto;

import com.winx.booking.domain.model.Booking;
import com.winx.booking.domain.model.BookingStatus;
import com.winx.booking.infrastructure.PaymentOutcome;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingResponse {

    private Long id;
    private Long userId;
    private Long vehicleId;
    private BookingStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double startLatitude;
    private Double startLongitude;
    private Double endLatitude;
    private Double endLongitude;
    private String vehicleType;
    private BigDecimal pricePerUnit;
    private String billingModel;
    private Double distanceKm;
    private BigDecimal totalCost;
    private Long paymentId;
    private String paymentStatus;

    public static BookingResponse from(Booking booking) {
        return from(booking, null);
    }

    public static BookingResponse from(Booking booking, PaymentOutcome paymentOutcome) {
        BookingResponse dto = new BookingResponse();
        dto.id = booking.getId();
        dto.userId = booking.getUserId();
        dto.vehicleId = booking.getVehicleSnapshot().getVehicleId();
        dto.status = booking.getStatus();
        dto.startTime = booking.getStartTime();
        dto.endTime = booking.getEndTime();
        dto.startLatitude = booking.getStartLatitude();
        dto.startLongitude = booking.getStartLongitude();
        dto.endLatitude = booking.getEndLatitude();
        dto.endLongitude = booking.getEndLongitude();
        dto.vehicleType = booking.getVehicleSnapshot().getType();
        dto.pricePerUnit = booking.getVehicleSnapshot().getPricePerUnit();
        dto.billingModel = booking.getVehicleSnapshot().getBillingModel();
        dto.distanceKm = booking.getRideSummary().getDistanceKm();
        dto.totalCost = booking.getRideSummary().getTotalCost();
        if (paymentOutcome != null) {
            dto.paymentId = paymentOutcome.paymentId();
            dto.paymentStatus = paymentOutcome.status();
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Double getStartLatitude() {
        return startLatitude;
    }

    public Double getStartLongitude() {
        return startLongitude;
    }

    public Double getEndLatitude() {
        return endLatitude;
    }

    public Double getEndLongitude() {
        return endLongitude;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public String getBillingModel() {
        return billingModel;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
}
