package ru.vilen.NailsServiceBot.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    final Long chatId;
    String userName;
    String phoneNumber;
    UserState userState;
}
