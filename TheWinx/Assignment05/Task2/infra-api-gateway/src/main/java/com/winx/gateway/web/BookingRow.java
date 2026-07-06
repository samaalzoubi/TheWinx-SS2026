package com.winx.gateway.web;

import com.winx.gateway.client.BookingClient.BookingResponse;
import com.winx.gateway.client.PaymentClient.PaymentResponse;
import com.winx.gateway.client.RatingClient.RatingResponse;

/**
 * View-model combining data from three different bounded contexts (Booking,
 * Payment, Rating) into one row for the dashboard - the whole point of this
 * portal being a single place to see "book, pay, rate" instead of three
 * separate per-service UIs.
 */
public record BookingRow(BookingResponse booking, PaymentResponse payment, RatingResponse rating) {

    public boolean isActive() {
        return "ACTIVE".equals(booking.status());
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(booking.status());
    }

    public boolean canBeRated() {
        return isCompleted() && rating == null;
    }
}
