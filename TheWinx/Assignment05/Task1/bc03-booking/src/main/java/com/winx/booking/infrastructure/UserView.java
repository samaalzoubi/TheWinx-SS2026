package com.winx.booking.infrastructure;

import java.time.LocalDate;

/**
 * Read-only projection of user data as seen from the Booking context.
 * Sourced from the Identity &amp; Access bounded context (mocked for now).
 */
public record UserView(Long id, String name, LocalDate dateOfBirth) {
}
