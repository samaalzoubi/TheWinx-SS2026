package com.winx.gateway.web;

import com.winx.gateway.client.BookingClient.BookingResponse;
import com.winx.gateway.client.PaymentClient.PaymentResponse;
import com.winx.gateway.client.RatingClient.RatingResponse;

// we combine booking/payment/rating into one row here since the whole point of this portal is one place to book, pay, and rate instead of three separate UIs
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
