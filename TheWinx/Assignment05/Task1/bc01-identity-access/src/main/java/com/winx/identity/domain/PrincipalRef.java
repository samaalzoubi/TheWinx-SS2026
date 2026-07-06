package com.winx.identity.domain;

/**
 * Reference to the principal (USER or PROVIDER) a validated token belongs to.
 */
public record PrincipalRef(Long id, String type) {

    public static final String USER = "USER";
    public static final String PROVIDER = "PROVIDER";
}
