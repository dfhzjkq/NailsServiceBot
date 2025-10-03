package ru.vilen.NailsServiceBot.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Setter
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    final Long chatId;
    String userName;
    String phoneNumber;
    LocalDate bookingDate;
    String bookingTime;
    UserState userState;
}
