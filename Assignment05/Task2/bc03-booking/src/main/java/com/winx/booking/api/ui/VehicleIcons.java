package com.winx.booking.api.ui;

// we match on substrings here so both bc02's real type names and our own shorter mock names map to an icon
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
