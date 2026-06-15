package com.winx.booking.domain;

/**
 * Lifecycle of a booking: ACTIVE -> COMPLETED or ACTIVE -> CANCELLED.
 * A COMPLETED booking can never become CANCELLED.
 */
public enum BookingStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}
