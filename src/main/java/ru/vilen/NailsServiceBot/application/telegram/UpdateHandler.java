package ru.vilen.NailsServiceBot.application.telegram;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UpdateHandler {

    CommandHandler commandHandler;
    CallbackHandler callbackHandler;
    MessageHandler registerHandler;

    void handle(Update update) {
        log.debug("[{}] Получен новый update!",
                update.getUpdateId());
        if (update.hasMessage()) {
            log.debug("[{}] В update прилетело сообщение!",
                    update.getUpdateId());
            if (update.getMessage().hasText()) {
                log.debug("[{}] В сообщении update прилетел текст!",
                        update.getUpdateId());
                if (update.getMessage().getText().startsWith("/")) {
                    log.debug("[{}] Текст сообщения update является командой!",
                            update.getUpdateId());
                    commandHandler.handle(update);
                } else {
                    registerHandler.handle(update);
                }
                return;
            }
        } else if (update.hasCallbackQuery()) {
            log.debug("[{}] В update прилетел callback!",
                    update.getUpdateId());
            callbackHandler.handle(update);
            return;
        }

        log.warn("[{}] В update что-то прилетело, но мы это не обработали!",
                update.getUpdateId());
    }
}
