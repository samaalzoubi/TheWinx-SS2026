package com.winx.identity.api;

import java.time.Instant;

public record UserLoginResponse(
        String token,
        Instant expiresAt,
        Long userId,
        String name,
        String email
) {
}
