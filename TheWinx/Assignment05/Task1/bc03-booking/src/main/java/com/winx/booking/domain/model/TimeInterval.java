package com.winx.booking.domain.model;

import java.time.LocalDateTime;

/**
 * Plain value object used in the cost-calculation service signature. Not a
 * JPA {@code @Embeddable} - {@link Booking} stores {@code startTime}/
 * {@code endTime} as plain fields since {@code endTime} must be nullable
 * until the ride finishes.
 */
public record TimeInterval(LocalDateTime start, LocalDateTime end) {
}
