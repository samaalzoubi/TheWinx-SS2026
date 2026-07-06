package com.winx.identity.api;

import com.winx.identity.domain.ProviderAccount;

import java.time.LocalDateTime;

public record ProviderResponse(
        Long id,
        String companyName,
        String contactName,
        String email,
        String phoneNumber,
        LocalDateTime registeredAt
) {
    public static ProviderResponse from(ProviderAccount account) {
        return new ProviderResponse(
                account.getId(),
                account.getCompanyInfo().getCompanyName(),
                account.getCompanyInfo().getContactName(),
                account.getEmail(),
                account.getPhoneNumber(),
                account.getRegisteredAt()
        );
    }
}
