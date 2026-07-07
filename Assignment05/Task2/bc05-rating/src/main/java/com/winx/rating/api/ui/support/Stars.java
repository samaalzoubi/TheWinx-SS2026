package com.winx.rating.api.ui.support;

// templates call this via SpringEL's T(...) static-method syntax, it's UI-only, never referenced from domain or REST code
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
