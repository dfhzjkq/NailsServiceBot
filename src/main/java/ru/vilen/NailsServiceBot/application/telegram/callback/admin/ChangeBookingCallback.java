package ru.vilen.NailsServiceBot.application.telegram.callback.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.vilen.NailsServiceBot.application.telegram.TelegramBot;
import ru.vilen.NailsServiceBot.application.telegram.callback.Callback;
import ru.vilen.NailsServiceBot.application.telegram.callback.CallbackType;
import ru.vilen.NailsServiceBot.entity.Booking;
import ru.vilen.NailsServiceBot.service.BookingService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChangeBookingCallback implements Callback {
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

        List<Booking> bookings = bookingService.getAllBookings();

        if (bookings.isEmpty()) {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text("Нет записей")
                    .build();
            bot.sendNewMessage(message);
            return;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        for (Booking book : bookings) {
            String formattedDate = book.getBookingDate().format(dateFormatter);
            String formattedTime = (book.getBookingTime() != null)
                    ? book.getBookingTime().format(timeFormatter)
                    : "—";

            String label = String.format("%s — %s (%s)",
                    book.getUser().getUserName(),
                    formattedDate,
                    formattedTime);

            buttons.add(List.of(
                    InlineKeyboardButton.builder()
                            .text(label)
                            .callbackData("SELECT_BOOKING_" + book.getId())
                            .build()
            ));
        }

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(buttons)
                .build();

        bot.sendNewMessage(SendMessage.builder()
                .chatId(chatId)
                .text("Выбери запись, которую хочешь изменить:")
                .replyMarkup(keyboard)
                .build());
    }

    @Override
    public CallbackType getType() {
        return CallbackType.CHANGE_BOOKING;
    }
}
