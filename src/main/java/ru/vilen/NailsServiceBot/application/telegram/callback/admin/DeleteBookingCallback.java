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
import ru.vilen.NailsServiceBot.entity.BookingType;
import ru.vilen.NailsServiceBot.entity.User;
import ru.vilen.NailsServiceBot.entity.UserStatus;
import ru.vilen.NailsServiceBot.service.BookingService;
import ru.vilen.NailsServiceBot.service.UserService;
import ru.vilen.NailsServiceBot.utils.AdminKeyboardUtils;
import ru.vilen.NailsServiceBot.utils.UserKeyboardUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeleteBookingCallback implements Callback {

    TelegramBot bot;
    BookingService bookingService;
    UserService userService;

    @Override
    public CallbackType getType() {
        return CallbackType.DELETE_BOOKING;
    }

    @Override
    public void apply(Update update) {

        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        log.debug("[DELETE_BOOKING] data={}", data);

        if (data.startsWith("SELECT_BOOKING_FOR_DELETE_")) {
            handleSelectBooking(data, chatId);
            return;
        }

        if (data.startsWith("YES_")) {
            handleDeleteWithNotification(data, chatId);
            return;
        }

        if (data.startsWith("NO_")) {
            handleDeleteWithoutNotification(data, chatId);
            return;
        }

        showBookingList(chatId);
    }

    private void showBookingList(Long chatId) {
        List<Booking> bookings = bookingService.getAllBookings();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Booking b : bookings) {
            String time = b.getBookingTime() == null ? "—" : b.getBookingTime().format(tf);
            String text = "%s — %s (%s)".formatted(
                    b.getUser().getUserName(),
                    b.getBookingDate().format(df),
                    time
            );

            rows.add(List.of(
                    InlineKeyboardButton.builder()
                            .text(text)
                            .callbackData("SELECT_BOOKING_FOR_DELETE_" + b.getId())
                            .build()
            ));
        }

        bot.sendNewMessage(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("Выберите запись, которую хотите удалить")
                        .replyMarkup(InlineKeyboardMarkup.builder().keyboard(rows).build())
                        .build()
        );
    }

    private void handleSelectBooking(String data, Long chatId) {
        Long bookingId = Long.parseLong(data.substring("SELECT_BOOKING_FOR_DELETE_".length()));

        bot.sendNewMessage(SendMessage.builder()
                .chatId(chatId)
                .text("Отправить сообщение с благодарностью и просьбой оставить отзыв?")
                .replyMarkup(AdminKeyboardUtils.buildDeleteBookingActionKeyboard(bookingId))
                .build());
    }

    private void handleDeleteWithoutNotification(String data, Long chatId) {
        Long bookingId = Long.parseLong(data.substring("NO_".length()));

        SendMessage adminMessage = SendMessage.builder()
                .chatId(chatId)
                .text("Запись удалена!")
                .build();

        bookingService.deleteBookingById(bookingId);
        bot.sendNewMessage(adminMessage);
    }

    private void handleDeleteWithNotification(String data, Long chatId) {
        Long bookingId = Long.parseLong(data.substring("YES_".length()));
        Long userChatId = bookingService.getBookingById(bookingId).getUser().getChatId();

        SendMessage adminMessage = SendMessage.builder()
                .chatId(chatId)
                .text("Запись удалена, уведомление отправлено!")
                .build();

        SendMessage userMessage = SendMessage.builder()
                .chatId(userChatId)
                .text(
                        "Спасибо, что выбрал наш сервис \uD83D\uDC85\n" +
                        "Нам важно, чтобы каждая процедура приносила только приятные впечатления.\n" +
                        "\n" +
                        "Если есть минутка — оставь, пожалуйста, отзыв на Авито:\n" +
                        "\uD83D\uDC49 https://www.avito.ru/schelkovo/predlozheniya_uslug/manikyur_pedikyur_na_domu_schelkovo_4516472544?utm_campaign=native&utm_medium=item_page_android&utm_source=soc_sharing\n" +
                        "\n" +
                        "Твоя оценка помогает нам развиваться и делает сервис лучше ✨")
                .replyMarkup(UserKeyboardUtils.buildDeleteBookingInlineKeyboard())
                .build();

        bookingService.deleteBookingById(bookingId);
        bot.sendNewMessage(adminMessage);
        bot.sendNewMessage(userMessage);
    }
}
