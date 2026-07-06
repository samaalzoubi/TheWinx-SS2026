package com.winx.booking.infrastructure;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;

/**
 * Feign client for the Identity &amp; Access bounded context (bc01), resolved
 * via Eureka service discovery (no hardcoded host/port).
 */
@FeignClient(name = "bc01-identity-access")
public interface IdentityFeignClient {

    @GetMapping("/api/users/{id}")
    IdentityUserResponse getUser(@PathVariable("id") Long id);

    record IdentityUserResponse(
            Long id,
            String name,
            String email,
            String phoneNumber,
            LocalDate dateOfBirth,
            String registeredAt) {
    }
}
