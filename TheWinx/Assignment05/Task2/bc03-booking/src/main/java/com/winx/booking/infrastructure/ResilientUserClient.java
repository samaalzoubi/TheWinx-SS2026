package com.winx.booking.infrastructure;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Real HTTP-backed {@link UserClient} that calls the Identity &amp; Access
 * service (bc01) via Feign/Eureka, guarded by a Resilience4j circuit breaker
 * named {@code identityClient}. Falls back to {@link MockUserClient}'s
 * in-memory seed data whenever the remote call fails technically (timeout,
 * connection refused, 5xx), so the service keeps behaving sensibly even when
 * Identity &amp; Access is unreachable.
 */
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
            // A legitimate "user does not exist" - not a technical failure, so
            // don't let it count against the circuit breaker's failure rate.
            return Optional.empty();
        }
    }

    // Must NOT be private: resilience4j-spring invokes fallback methods via
    // reflection on the AOP proxy itself, and a private method bypasses the
    // proxy's target delegation, leaving injected fields null (see
    // https://github.com/resilience4j/resilience4j/issues/1993).
    public Optional<UserView> getUserFallback(Long userId, Throwable t) {
        log.warn("Identity client circuit breaker fallback for user {}: {}", userId, t.toString());
        return fallbackClient.getUser(userId);
    }
}
