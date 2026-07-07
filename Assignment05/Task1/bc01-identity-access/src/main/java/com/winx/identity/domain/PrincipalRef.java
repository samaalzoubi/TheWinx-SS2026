package com.winx.identity.domain;

public record PrincipalRef(Long id, String type) {

    public static final String USER = "USER";
    public static final String PROVIDER = "PROVIDER";
}
