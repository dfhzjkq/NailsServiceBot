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
import ru.vilen.NailsServiceBot.application.telegram.callback.CallbackType;
import ru.vilen.NailsServiceBot.config.TelegramBotProperties;
import ru.vilen.NailsServiceBot.service.BookingService;
import ru.vilen.NailsServiceBot.utils.UserKeyboardUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingCallback implements Callback {

    TelegramBotProperties properties;
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
                .text(String.format("✨ Выбери тип услуги:\n\n" +
                        "💅 *Маникюр*\n" +
                        "└ Покрытие гель-лаком — 1 ч 45 мин. — 1 700 ₽\n\n" +
                        "🦶 *Педикюр*\n" +
                        "└ Обработка стоп и пальцев + покрытие гель-лаком — 1 ч 45 мин. — \n2 200 ₽\n\n" +
                        "💅+🦶 *Маникюр + Педикюр*\n" +
                        "└ Комплекс с покрытием — 3 ч 30 мин. — 3 900 ₽\n\n" +
                        "➕ *Дополнительные услуги:*\n" +
                        "├ Ремонт ногтя — 150 ₽\n" +
                        "└ Маникюр без покрытия — 700 ₽\n\n" +
                        "📩 _Для записи на доп. услуги — напиши мастеру:_ %s", properties.getAdminLink()))
                .parseMode("Markdown")
                .replyMarkup(UserKeyboardUtils.buildBookInlineKeyboard())
                .build();

        bot.sendNewMessage(message);
    }

    @Override
    public CallbackType getType() {
        return CallbackType.BOOK;
    }
}
