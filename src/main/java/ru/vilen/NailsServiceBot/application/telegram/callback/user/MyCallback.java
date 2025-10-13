package ru.vilen.NailsServiceBot.application.telegram.callback;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.vilen.NailsServiceBot.application.telegram.TelegramBot;
import ru.vilen.NailsServiceBot.entity.User;
import ru.vilen.NailsServiceBot.service.UserService;
import ru.vilen.NailsServiceBot.utils.UserKeyboardUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MyCallback implements Callback {

    TelegramBot bot;
    UserService userService;

    @Override
    public void apply(Update update) {
        Long userId = update.getCallbackQuery().getFrom().getId();
        String userName = update.getCallbackQuery().getFrom().getUserName();
        log.info("[{}] Callback {} от пользователя {} [id{}]",
                update.getUpdateId(),
                getType(),
                userName,
                userId);

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        User user = userService.getOrCreateUser(chatId);

        if (user.getBookingDate() == null) {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text("✨ Сейчас у тебя нет действующих записей.  \n" +
                            "Запишись на процедуру, когда будет удобно \uD83D\uDC85")
                    .replyMarkup(UserKeyboardUtils.buildHomeInlineKeyboard())
                    .build();

            bot.sendNewMessage(message);
            return;
        }

        String date = user.getBookingDate().toString();
        String time = user.getBookingTime();

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(String.format("""
                        💅 Твоя действующая запись:
                        %s на %s
                        """, date, time))
                .replyMarkup(UserKeyboardUtils.buildMyInlineKeyboard())
                .build();

        bot.sendNewMessage(message);
    }

    @Override
    public CallbackType getType() {
        return CallbackType.MY;
    }
}
