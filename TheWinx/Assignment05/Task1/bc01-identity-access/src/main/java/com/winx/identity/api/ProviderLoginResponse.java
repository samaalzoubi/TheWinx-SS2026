package com.winx.identity.api;

import java.time.Instant;

public record ProviderLoginResponse(
        String token,
        Instant expiresAt,
        Long providerId,
        String companyName,
        String email
) {
}
