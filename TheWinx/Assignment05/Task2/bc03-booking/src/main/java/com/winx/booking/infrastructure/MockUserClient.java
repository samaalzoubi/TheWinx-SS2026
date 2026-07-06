package com.winx.booking.infrastructure;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory stand-in for the Identity &amp; Access bounded context. Seeded
 * with a handful of example users so the Booking service can be exercised
 * fully standalone.
 */
@Component
public class MockUserClient implements UserClient {

    private final Map<Long, UserView> users = new HashMap<>();

    public MockUserClient() {
        users.put(1L, new UserView(1L, "Alice Example", LocalDate.of(1995, 5, 20)));
        users.put(2L, new UserView(2L, "Bob Example", LocalDate.of(2005, 1, 10))); // deliberately young
        users.put(3L, new UserView(3L, "Carla Example", LocalDate.of(1988, 3, 2)));
    }

    @Override
    public Optional<UserView> getUser(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }
}
