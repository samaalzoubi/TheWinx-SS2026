package com.winx.booking.infrastructure.client;

import com.winx.booking.api.dto.PrincipalDto;

/** Booking's anti-corruption gateway to Identity &amp; Access. */
public interface IdentityGateway {

    /** Validates the auth token and returns the authenticated principal. */
    PrincipalDto validate(String token);
}
