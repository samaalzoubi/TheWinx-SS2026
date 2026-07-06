package com.winx.identity.domain;

import java.util.regex.Pattern;

/**
 * Small stateless validation helper for the Email value.
 * Email itself is modeled as a plain String column (unique, not-null) on the
 * aggregates rather than as a separate {@code @Embeddable}, since the only
 * behaviour it needs is format validation.
 */
public final class EmailValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}
