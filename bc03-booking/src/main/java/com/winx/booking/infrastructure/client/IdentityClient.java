package com.winx.booking.infrastructure.client;

import com.winx.booking.api.dto.PrincipalDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Declarative REST client for Identity &amp; Access (BC-01). The service id is resolved via Eureka.
 * Only created when not running under the {@code mock} profile.
 */
@FeignClient(name = "bc01-identity-access", path = "/auth")
public interface IdentityClient {

    @GetMapping("/validate")
    PrincipalDto validate(@RequestHeader("X-Auth-Token") String token);
}
