package com.winx.rating.api.ui.support;

// we call this from templates via SpringEL's T(...) syntax, so it looks unused if you only grep the Java code
public final class Stars {

    private static final int MAX = 5;

    private Stars() {
    }

    public static String render(Integer score) {
        int filled = score == null ? 0 : Math.max(0, Math.min(MAX, score));
        int empty = MAX - filled;
        return "★".repeat(filled) + "☆".repeat(empty);
    }
}
