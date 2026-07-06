package com.winx.identity.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record UserRegisterRequest(
        @NotBlank(message = "name is required") String name,
        @NotBlank(message = "email is required") String email,
        @NotBlank(message = "password is required") String password,
        String phoneNumber,
        @NotNull(message = "dateOfBirth is required") @Past(message = "dateOfBirth must be in the past") LocalDate dateOfBirth
) {
}
