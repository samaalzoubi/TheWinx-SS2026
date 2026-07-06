package com.winx.rating.api.ui.support;

/**
 * Tiny Thymeleaf-friendly helper turning a 1-5 integer score into a
 * "★★★☆☆" style string. Invoked from templates via the SpringEL
 * {@code T(...)} static-method syntax, e.g.
 * {@code ${T(com.winx.rating.api.ui.support.Stars).render(rating.review.vehicleScore)}}.
 *
 * UI-layer only - never referenced from the domain or REST API.
 */
public final class Stars {

    private static final int MAX = 5;

    private Stars() {
        // utility class
    }

    public static String render(Integer score) {
        int filled = score == null ? 0 : Math.max(0, Math.min(MAX, score));
        int empty = MAX - filled;
        return "★".repeat(filled) + "☆".repeat(empty);
    }
}
