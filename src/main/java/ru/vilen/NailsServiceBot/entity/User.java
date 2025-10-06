package ru.vilen.NailsServiceBot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @Column(name = "chat_id")
    Long chatId;

    @Column(name = "user_name")
    String userName;

    @Column(name = "phone_number")
    String phoneNumber;

    @Column(name = "booking_date")
    LocalDate bookingDate;

    @Column(name = "booking_time")
    String bookingTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_state")
    UserState userState;
}
