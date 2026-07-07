package com.winx.booking.domain.service;

import com.winx.booking.domain.model.VehicleSnapshot;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class CostCalculationService {

    public BigDecimal computeCost(VehicleSnapshot snapshot, LocalDateTime start, LocalDateTime end, double distanceKm) {
        String billingModel = snapshot.getBillingModel();
        BigDecimal pricePerUnit = snapshot.getPricePerUnit();

        if ("PER_HOUR".equalsIgnoreCase(billingModel)) {
            long minutes = Duration.between(start, end).toMinutes();
            long hoursStarted = (long) Math.ceil(minutes / 60.0);
            if (hoursStarted < 1) {
                hoursStarted = 1;
            }
            return pricePerUnit.multiply(BigDecimal.valueOf(hoursStarted)).setScale(2, RoundingMode.HALF_UP);
        } else if ("PER_KILOMETER".equalsIgnoreCase(billingModel)) {
            return pricePerUnit.multiply(BigDecimal.valueOf(distanceKm)).setScale(2, RoundingMode.HALF_UP);
        }
        throw new IllegalStateException("Unknown billing model: " + billingModel);
    }

    public double computeDistanceKm(double startLat, double startLon, double endLat, double endLon) {
        return GeoMath.haversineKm(startLat, startLon, endLat, endLon);
    }
}
