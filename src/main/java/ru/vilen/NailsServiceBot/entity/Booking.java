package ru.vilen.NailsServiceBot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "users")
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
    @Column(name = "status")
    BookingStatus status = BookingStatus.PENDING;
}
