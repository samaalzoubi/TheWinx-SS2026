package com.winx.booking.api.ui;

/**
 * UI-only helper mapping a vehicle type string to a display emoji icon.
 * Not a domain concept - purely presentational, used from Thymeleaf
 * templates via a static call (e.g. {@code T(...).iconFor(v.type)}).
 * <p>
 * Handles both the canonical Fleet (bc02) type names (E_SCOOTER, BICYCLE,
 * E_BIKE, E_CAR) and the shorter names used by this service's standalone
 * mock vehicle data (SCOOTER, BIKE, CAR), so the icon shows up correctly
 * whether bc02 is running or not.
 */
public final class VehicleIcons {

    private VehicleIcons() {
    }

    public static String iconFor(String type) {
        if (type == null || type.isBlank()) {
            return "🚘";
        }
        String t = type.toUpperCase();
        if (t.contains("SCOOTER")) {
            return "🛴"; // 🛴
        }
        if (t.contains("E_BIKE") || t.contains("EBIKE")) {
            return "⚡"; // ⚡
        }
        if (t.contains("BIKE") || t.contains("BICYCLE")) {
            return "🚲"; // 🚲
        }
        if (t.contains("CAR")) {
            return "🚗"; // 🚗
        }
        return "🚘";
    }
}
