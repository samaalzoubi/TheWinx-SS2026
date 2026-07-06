package com.winx.gateway.web;

/** Renders a 1-5 integer score as filled/empty star glyphs for templates. */
public final class Stars {
    private Stars() {
    }

    public static String render(Integer score) {
        if (score == null) {
            return "-";
        }
        int filled = Math.max(0, Math.min(5, score));
        return "★".repeat(filled) + "☆".repeat(5 - filled);
    }
}
