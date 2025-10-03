package ru.vilen.NailsServiceBot.application.telegram.callback;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.vilen.NailsServiceBot.application.telegram.CallbackHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefaultCallbackHandler implements CallbackHandler {

    Map<CallbackType, Callback> callbacksMap;

    @Autowired
    public DefaultCallbackHandler(List<Callback> callbacks) {
        this.callbacksMap = new HashMap<>();
        callbacks.forEach(callback -> callbacksMap.put(callback.getType(), callback));
    }

    @Override
    public void handle(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        log.debug("[{}] Получен callback {}",
                update.getUpdateId(),
                callbackData);

        CallbackType type;

        if (callbackData.startsWith("DATE_")) {
            type = CallbackType.DATE;
        } else if (callbackData.startsWith("CAL_PREV_") || callbackData.startsWith("CAL_NEXT_")) {
            type = CallbackType.DATE;
        } else {
            type = CallbackType.valueOf(callbackData);
        }

        Callback callback = callbacksMap.get(type);
        if (callback != null) {
            callback.apply(update);
        } else {
            Long userId = update.getCallbackQuery().getFrom().getId();
            String userName = update.getCallbackQuery().getFrom().getUserName();
            log.error (
                    "[{}] Неизвестный callback {} от пользователя {} [id={}]",
                    update.getUpdateId(),
                    callbackData,
                    userName,
                    userId
            );
        }
    }
}
