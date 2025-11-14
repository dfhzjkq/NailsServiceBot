package ru.vilen.NailsServiceBot.entity;

public enum BookingType {
    MANICURE("💅 Маникюр", 2),
    PEDICURE("🦶 Педикюр", 2),
    BOTH("💅🦶 Маникюр и педикюр", 4);

    private final String label;
    private final int duration;

    BookingType(String label, int duration) {
        this.label = label;
        this.duration = duration;
    }

    public String getLabel() {
        return label;
    }

    public int getDuration() {
        return duration;
    }
}
