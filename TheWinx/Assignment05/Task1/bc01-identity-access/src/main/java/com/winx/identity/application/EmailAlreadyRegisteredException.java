package com.winx.identity.application;

/**
 * Thrown when a registration attempt uses an email that is already
 * associated with an existing UserAccount or ProviderAccount.
 * Mapped to HTTP 409 Conflict by the GlobalExceptionHandler.
 */
public class EmailAlreadyRegisteredException extends RuntimeException {

    public EmailAlreadyRegisteredException(String email) {
        super("Email already registered: " + email);
    }
}
