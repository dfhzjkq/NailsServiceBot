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
import ru.vilen.NailsServiceBot.entity.Booking;
import ru.vilen.NailsServiceBot.service.BookingService;
import ru.vilen.NailsServiceBot.utils.UserKeyboardUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MyCallback implements Callback {

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

        Booking book = null;
        try {
            book = bookingService.getActiveBooking(chatId);
        } catch (Exception e) {
            log.error("Ошибка при получении активной записи для chatId[{}]: {}", chatId, e.getMessage());
        }

        if (book == null) {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text("✨ Сейчас у тебя нет действующих записей.  \n" +
                            "Запишись на процедуру, когда будет удобно \uD83D\uDC85")
                    .replyMarkup(UserKeyboardUtils.buildHomeInlineKeyboard())
                    .build();

            bot.sendNewMessage(message);
            return;
        }

        String date = bookingService.formatTheDate(book.getBookingDate());
        String time = book.getBookingTime().toString();

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(String.format("""
                        💅 Твоя действующая запись:
                        
                        📅 Дата: %s
                        ⏰ Время: %s
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
