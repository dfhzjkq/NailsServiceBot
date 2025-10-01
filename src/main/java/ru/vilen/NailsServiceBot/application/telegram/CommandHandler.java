package ru.vilen.NailsServiceBot.application.telegram;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface CommandHandler {

    void handle(Update update);
}
