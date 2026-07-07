package com.winx.booking.infrastructure;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class MockUserClient implements UserClient {

    private final Map<Long, UserView> users = new HashMap<>();

    public MockUserClient() {
        users.put(1L, new UserView(1L, "Marianne Nosseir", LocalDate.of(1994, 7, 19)));
        users.put(2L, new UserView(2L, "Rowena Pagayanan", LocalDate.of(1990, 3, 25)));
        users.put(3L, new UserView(3L, "Priyanka Gupta", LocalDate.of(1998, 11, 8)));
    }

    @Override
    public Optional<UserView> getUser(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }
}
