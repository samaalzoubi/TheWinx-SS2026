package com.winx.identity.domain;

import java.time.Instant;

// we don't persist this as a JPA entity - tokens just live in AuthenticationService's in-memory store for the lab
public record AuthToken(String value, Instant expiresAt) {

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
