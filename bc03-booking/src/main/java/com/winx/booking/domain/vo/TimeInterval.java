package com.winx.booking.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Immutable time span of a booking. {@code endTime} is null while the ride is ACTIVE
 * and is set (replacing the whole value object) when the ride ends.
 */
@Embeddable
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeInterval {

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    public static TimeInterval startingNow() {
        return new TimeInterval(LocalDateTime.now(), null);
    }

    public TimeInterval endingAt(LocalDateTime endTime) {
        return new TimeInterval(this.startTime, endTime);
    }
}
