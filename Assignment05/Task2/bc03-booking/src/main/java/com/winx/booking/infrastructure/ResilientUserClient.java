package com.winx.booking.infrastructure;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

// we fall back to MockUserClient's seed data here so we keep behaving sensibly even when Identity & Access is unreachable
@Component
@Primary
public class ResilientUserClient implements UserClient {

    private static final Logger log = LoggerFactory.getLogger(ResilientUserClient.class);

    private final IdentityFeignClient feignClient;
    private final MockUserClient fallbackClient;

    public ResilientUserClient(IdentityFeignClient feignClient, MockUserClient fallbackClient) {
        this.feignClient = feignClient;
        this.fallbackClient = fallbackClient;
    }

    @Override
    @CircuitBreaker(name = "identityClient", fallbackMethod = "getUserFallback")
    public Optional<UserView> getUser(Long userId) {
        try {
            IdentityFeignClient.IdentityUserResponse r = feignClient.getUser(userId);
            return Optional.of(new UserView(r.id(), r.name(), r.dateOfBirth()));
        } catch (FeignException.NotFound e) {
            // a legitimate "user does not exist", we don't want this counting against the breaker's failure rate
            return Optional.empty();
        }
    }

    // we kept this non-private since resilience4j-spring invokes fallbacks via reflection on the AOP proxy, a private method would bypass that and leave our injected fields null
    public Optional<UserView> getUserFallback(Long userId, Throwable t) {
        log.warn("Identity client circuit breaker fallback for user {}: {}", userId, t.toString());
        return fallbackClient.getUser(userId);
    }
}
