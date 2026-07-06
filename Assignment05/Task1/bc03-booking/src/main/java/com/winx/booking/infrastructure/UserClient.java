package com.winx.booking.infrastructure;

import java.util.Optional;

/**
 * Gateway to the Identity &amp; Access bounded context. Implemented today by
 * {@link MockUserClient} with hardcoded example data so that this service
 * runs fully standalone; a later integration phase can add a real
 * HTTP-based implementation without touching any calling code.
 */
public interface UserClient {

    Optional<UserView> getUser(Long userId);
}
