package ru.vilen.NailsServiceBot.entity;

import java.time.Duration;

public enum BookingType {
    MANICURE("💅 Маникюр", 120),
    PEDICURE("🦶 Педикюр", 120),
    BOTH("💅🦶 Маникюр и педикюр", 210);

    private final String label;
    private final int durationMinutes;

    BookingType(String label, int durationMinutes) {
        this.label = label;
        this.durationMinutes = durationMinutes;
    }

    public String getLabel() {
        return label;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public Duration getDuration() {
        return Duration.ofMinutes(durationMinutes);
    }
}