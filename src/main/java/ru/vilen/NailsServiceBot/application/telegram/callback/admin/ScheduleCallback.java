package ru.vilen.NailsServiceBot.application.telegram.callback.admin;

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
import ru.vilen.NailsServiceBot.entity.BookingStatus;
import ru.vilen.NailsServiceBot.service.BookingService;
import ru.vilen.NailsServiceBot.utils.AdminKeyboardUtils;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScheduleCallback implements Callback {
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

        List<Booking> bookings = bookingService.getAllBookings().stream()
                .filter(b -> b.getBookingTime() != null)
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .toList();

        if (bookings.isEmpty()) {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text("📭 Пока нет активных записей!")
                    .replyMarkup(AdminKeyboardUtils.buildScheduleInlineKeyboard())
                    .build();

            bot.sendNewMessage(message);
            return;
        }

        StringBuilder sb = new StringBuilder("📅 Все записи:\n\n");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (Booking book : bookings) {

            Duration duration = book.getBookingType().getDuration();
            String procedure = book.getBookingType().getLabel();

            String start = book.getBookingTime().format(timeFormatter);
            String end = book.getBookingTime().plus(duration).format(timeFormatter);

            String line = String.format("""
            <blockquote>%s</blockquote>
            <b>%s — %s</b>
            Клиент: %s
            Телефон: <code>%s</code>
            Услуга: %s
            Ссылка: @%s

            """,
                book.getBookingDate().format(dateFormatter),
                start,
                end,
                book.getUser().getUserName(),
                book.getUser().getPhoneNumber(),
                procedure,
                book.getUser().getUserLink()
            );

            sb.append(line);
        }

        SendMessage bookingsMessage = SendMessage.builder()
                .chatId(chatId)
                .text(sb.toString())
                .replyMarkup(AdminKeyboardUtils.buildScheduleInlineKeyboard())
                .parseMode("HTML")
                .build();

        bot.sendNewMessage(bookingsMessage);
    }

    @Override
    public CallbackType getType() {
        return CallbackType.SCHEDULE;
    }
}

