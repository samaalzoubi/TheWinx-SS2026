package com.winx.identity.api;

public record ValidateTokenResponse(
        boolean valid,
        Long principalId,
        String principalType
) {
    public static ValidateTokenResponse invalid() {
        return new ValidateTokenResponse(false, null, null);
    }
}
