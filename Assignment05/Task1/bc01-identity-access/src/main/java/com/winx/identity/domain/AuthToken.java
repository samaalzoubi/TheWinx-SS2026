package com.winx.identity.domain;

import java.time.Instant;

/**
 * Value Object representing an opaque authentication token.
 * Not persisted as its own JPA entity - active tokens live in an in-memory
 * store managed by the AuthenticationService for the lifetime of this lab.
 */
public record AuthToken(String value, Instant expiresAt) {

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
