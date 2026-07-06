package com.winx.identity.api;

import jakarta.validation.constraints.NotBlank;

/**
 * Shared login request shape for both Users and Providers.
 */
public record LoginRequest(
        @NotBlank(message = "email is required") String email,
        @NotBlank(message = "password is required") String password
) {
}
