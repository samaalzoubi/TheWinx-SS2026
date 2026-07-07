package com.winx.booking.infrastructure;

import java.util.Optional;

// same reasoning as PaymentClient, an interface so a real HTTP implementation can slot in later without touching callers
public interface UserClient {

    Optional<UserView> getUser(Long userId);
}
