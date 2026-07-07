package com.winx.booking.domain.model;

// we kept this as a plain record, not a JPA @Embeddable, since Booking needs two independent pairs (start/end) and four plain fields avoid @AttributeOverrides boilerplate
public record RideLocation(double latitude, double longitude) {
}
