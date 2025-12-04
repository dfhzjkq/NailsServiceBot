package ru.vilen.NailsServiceBot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "bookings")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "chat_id", referencedColumnName = "chat_id")
    User user;

    @Column(name = "date")
    LocalDate bookingDate;

    @Column(name = "time")
    LocalTime bookingTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_type", nullable = false)
    BookingType bookingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    BookingStatus status;

    @Column(name = "reminder_sent", nullable = false)
    Boolean reminderSent = false;

    public Duration getDuration() {
        return bookingType != null ? bookingType.getDuration() : Duration.ZERO;
    }
}
