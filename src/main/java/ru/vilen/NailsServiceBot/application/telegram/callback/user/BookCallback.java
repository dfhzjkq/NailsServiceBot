package ru.vilen.NailsServiceBot.application.telegram.callback.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.vilen.NailsServiceBot.application.telegram.TelegramBot;
import ru.vilen.NailsServiceBot.application.telegram.callback.Callback;
import ru.vilen.NailsServiceBot.service.BookingService;
import ru.vilen.NailsServiceBot.utils.UserKeyboardUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookCallback implements Callback {

    TelegramBot bot;
    BookingService bookingService;

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

        bookingService.clearUnfinishedBooking(chatId);

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("✨Отлично! Что будем делать дальше?\n" +
                        "Выбери нужное действие ниже ⬇\uFE0F")
                .replyMarkup(UserKeyboardUtils.buildBookInlineKeyboard())
                .build();

        bot.sendNewMessage(message);
    }

    @Override
    public UserCallbackType getType() {
        return UserCallbackType.BOOK;
    }
}
