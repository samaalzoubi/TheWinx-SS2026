package com.winx.identity.api;

import jakarta.validation.constraints.NotBlank;

public record ProviderRegisterRequest(
        @NotBlank(message = "companyName is required") String companyName,
        @NotBlank(message = "contactName is required") String contactName,
        @NotBlank(message = "email is required") String email,
        @NotBlank(message = "password is required") String password,
        String phoneNumber
) {
}
