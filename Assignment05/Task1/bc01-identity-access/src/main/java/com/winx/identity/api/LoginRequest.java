package com.winx.identity.api;

import jakarta.validation.constraints.NotBlank;

// we reuse this same request shape for both User and Provider logins since the fields are identical
public record LoginRequest(
        @NotBlank(message = "email is required") String email,
        @NotBlank(message = "password is required") String password
) {
}
