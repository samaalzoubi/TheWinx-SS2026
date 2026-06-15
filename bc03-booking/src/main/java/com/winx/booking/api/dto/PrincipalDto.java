package com.winx.booking.api.dto;

import java.time.LocalDate;

/**
 * Authenticated party returned by Identity &amp; Access {@code GET /auth/validate}.
 * Booking's ACL view of a User: only id, kind, email and date of birth (for restriction checks).
 */
public record PrincipalDto(
        Long id,
        String kind,          // USER | PROVIDER
        String email,
        LocalDate dateOfBirth
) {
}
