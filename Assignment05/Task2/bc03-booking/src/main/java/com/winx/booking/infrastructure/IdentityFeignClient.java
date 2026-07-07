package com.winx.booking.infrastructure;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;

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
