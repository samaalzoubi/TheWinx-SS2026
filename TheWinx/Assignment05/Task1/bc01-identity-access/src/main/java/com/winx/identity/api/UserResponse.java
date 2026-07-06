package com.winx.identity.api;

import com.winx.identity.domain.UserAccount;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phoneNumber,
        LocalDate dateOfBirth,
        LocalDateTime registeredAt
) {
    public static UserResponse from(UserAccount account) {
        return new UserResponse(
                account.getId(),
                account.getPersonalInfo().getName(),
                account.getEmail(),
                account.getPersonalInfo().getPhoneNumber(),
                account.getPersonalInfo().getDateOfBirth(),
                account.getRegisteredAt()
        );
    }
}
