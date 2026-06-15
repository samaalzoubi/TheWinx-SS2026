package com.winx.booking.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Immutable result of cost computation, set when a ride is COMPLETED.
 * Null until then. (Cost computation itself is implemented in a later pass.)
 */
@Embeddable
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RideSummary {

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "total_cost")
    private BigDecimal totalCost;
}
