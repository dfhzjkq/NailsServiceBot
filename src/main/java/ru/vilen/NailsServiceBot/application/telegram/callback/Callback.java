package ru.vilen.NailsServiceBot.application.telegram.callback;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.vilen.NailsServiceBot.application.telegram.callback.user.UserCallbackType;

public interface Callback {

    void apply(Update update);

    UserCallbackType getType();
}
