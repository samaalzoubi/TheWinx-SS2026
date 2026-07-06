package com.winx.booking.domain.model;

/**
 * Plain value object representing a geographic coordinate pair. Not a JPA
 * {@code @Embeddable} on purpose - {@link Booking} needs two independent
 * coordinate pairs (start/end) and modelling them as four plain fields on the
 * entity avoids {@code @AttributeOverrides} boilerplate while this type is
 * still used as a proper value object across service method signatures.
 */
public record RideLocation(double latitude, double longitude) {
}
