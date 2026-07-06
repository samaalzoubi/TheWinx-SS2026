package com.winx.gateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "bc01-identity-access")
public interface IdentityClient {

    @PostMapping("/api/users/register")
    UserResponse register(@RequestBody RegisterRequest request);

    @PostMapping("/api/users/login")
    LoginResponse login(@RequestBody LoginRequest request);

    @PostMapping("/api/providers/register")
    ProviderResponse registerProvider(@RequestBody RegisterProviderRequest request);

    @PostMapping("/api/providers/login")
    ProviderLoginResponse loginProvider(@RequestBody LoginRequest request);

    record RegisterRequest(String name, String email, String password, String phoneNumber, String dateOfBirth) {
    }

    record RegisterProviderRequest(String companyName, String contactName, String email, String password, String phoneNumber) {
    }

    record LoginRequest(String email, String password) {
    }

    record UserResponse(Long id, String name, String email, String phoneNumber, String dateOfBirth, String registeredAt) {
    }

    record LoginResponse(String token, String expiresAt, Long userId, String name, String email) {
    }

    record ProviderResponse(Long id, String companyName, String contactName, String email, String phoneNumber, String registeredAt) {
    }

    record ProviderLoginResponse(String token, String expiresAt, Long providerId, String companyName, String email) {
    }
}
