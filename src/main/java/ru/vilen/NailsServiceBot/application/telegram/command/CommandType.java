package ru.vilen.NailsServiceBot.application.telegram.command;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum CommandType {

    START("/start", "Зарегистрироваться");

    String name;
    String description;
}
