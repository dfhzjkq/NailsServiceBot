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
import ru.vilen.NailsServiceBot.config.TelegramBotProperties;
import ru.vilen.NailsServiceBot.entity.Booking;
import ru.vilen.NailsServiceBot.entity.BookingType;
import ru.vilen.NailsServiceBot.entity.User;
import ru.vilen.NailsServiceBot.entity.UserStatus;
import ru.vilen.NailsServiceBot.service.BookingService;
import ru.vilen.NailsServiceBot.service.UserService;
import ru.vilen.NailsServiceBot.utils.AdminKeyboardUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChangeBookingCallback implements Callback {

    TelegramBotProperties properties;
    TelegramBot bot;
    BookingService bookingService;
    UserService userService;

    @Override
    public CallbackType getType() {
        return CallbackType.CHANGE_BOOKING;
    }

    @Override
    public void apply(Update update) {

        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        log.debug("[CHANGE_BOOKING] data={}", data);

        if (data.startsWith("SELECT_BOOKING_")) {
            handleSelectBooking(data, chatId);
            return;
        }

        if (data.startsWith("CHANGE_DATE_")) {
            handleStartChangeDate(data, chatId);
            return;
        }

        if (data.startsWith("ADMIN_CAL_PREV_")) {
            handleCalendarPrev(data, chatId);
            return;
        }

        if (data.startsWith("ADMIN_CAL_NEXT_")) {
            handleCalendarNext(data, chatId);
            return;
        }

        if (data.startsWith("SET_NEW_DATE_")) {
            handleApplyNewDate(data, chatId);
            return;
        }

        if (data.startsWith("CHANGE_TIME_")) {
            handleChangeTime(data, chatId);
            return;
        }

        if (data.startsWith("CHANGE_TYPE_")) {
            handleStartChangeType(data, chatId);
            return;
        }

        if (data.startsWith("ADMIN_TYPE_")) {
            handleApplyNewType(data, chatId);
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
                            .callbackData("SELECT_BOOKING_" + b.getId())
                            .build()
            ));
        }

        bot.sendNewMessage(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("\uD83D\uDCDD Выберите запись, которую хотите изменить:")
                        .replyMarkup(InlineKeyboardMarkup.builder().keyboard(rows).build())
                        .build()
        );
    }

    private void handleSelectBooking(String data, Long chatId) {
        Long bookingId = Long.parseLong(data.substring("SELECT_BOOKING_".length()));

        bot.sendNewMessage(SendMessage.builder()
                .chatId(chatId)
                .text("⚙\uFE0F Выберите действие с записью:")
                .replyMarkup(AdminKeyboardUtils.buildChangeBookingActionKeyboard(bookingId))
                .build());
    }

    private void handleStartChangeDate(String data, Long chatId) {
        Long bookingId = Long.parseLong(data.substring("CHANGE_DATE_".length()));

        bot.sendNewMessage(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("\uD83D\uDCC5 Выберите новую дату для записи:")
                        .replyMarkup(AdminKeyboardUtils.buildCalendar(bookingId, LocalDate.now()))
                        .build()
        );
    }

    private void handleCalendarPrev(String data, Long chatId) {
        // ADMIN_CAL_PREV_<bookingId>_<yyyy-MM>
        String[] p = data.split("_");
        Long bookingId = Long.parseLong(p[3]);
        YearMonth ym = YearMonth.parse(p[4]);

        LocalDate newMonth = ym.minusMonths(1).atDay(1);

        bot.sendNewMessage(SendMessage.builder()
                .chatId(chatId)
                .text("\uD83D\uDCC5 Выберите новую дату для записи:")
                .replyMarkup(AdminKeyboardUtils.buildCalendar(bookingId, newMonth))
                .build());
    }

    private void handleCalendarNext(String data, Long chatId) {
        // ADMIN_CAL_NEXT_<bookingId>_<yyyy-MM>
        String[] p = data.split("_");
        Long bookingId = Long.parseLong(p[3]);
        YearMonth ym = YearMonth.parse(p[4]);

        LocalDate newMonth = ym.plusMonths(1).atDay(1);

        bot.sendNewMessage(SendMessage.builder()
                .chatId(chatId)
                .text("\uD83D\uDCC5 Выберите новую дату для записи:")
                .replyMarkup(AdminKeyboardUtils.buildCalendar(bookingId, newMonth))
                .build());
    }

    private void handleApplyNewDate(String data, Long chatId) {
        // SET_NEW_DATE_<bookingId>_<yyyy-MM-dd>
        String[] p = data.split("_");

        Long bookingId = Long.parseLong(p[3]);
        LocalDate newDate = LocalDate.parse(p[4]);

        Booking booking = bookingService.getBookingById(bookingId);
        booking.setBookingDate(newDate);
        bookingService.saveBooking(booking);

        sendNotificationToUser(booking);

        bot.sendNewMessage(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("✅ Дата записи успешно изменена!")
                        .build()
        );
    }

    private void handleChangeTime(String data, Long chatId) {
        String[] p = data.split("_");

        Long bookingId = Long.parseLong(p[2]);
        Booking booking = bookingService.getBookingById(bookingId);
        User user = booking.getUser();

        user.setUserState(UserStatus.WAITING_NEW_TIME);
        userService.saveUser(user);

        bot.sendNewMessage(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("⏰ Введите новое время для записи:")
                        .build()
        );
    }

    private void handleStartChangeType(String data, Long chatId) {
        Long bookingId = Long.parseLong(data.substring("CHANGE_TYPE_".length()));

        bot.sendNewMessage(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("✨ Выберите тип услуги:")
                        .replyMarkup(AdminKeyboardUtils.buildChangeTypeInlineKeyboard(bookingId))
                        .build()
        );
    }

    private void handleApplyNewType(String data, Long chatId) {
        String[] p = data.split("_");

        Long bookingId = Long.parseLong(p[3]);
        Booking booking = bookingService.getBookingById(bookingId);

        booking.setBookingType(BookingType.valueOf(p[2]));
        bookingService.saveBooking(booking);

        sendNotificationToUser(booking);

        bot.sendNewMessage(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("✅ Тип услуги успешно изменён!")
                        .build()
        );
    }

    private void sendNotificationToUser(Booking booking) {
        SendMessage message = SendMessage.builder()
                .chatId(booking.getUser().getChatId())
                .text(String.format("⚠\uFE0F Важное обновление по твоей записи!\n" +
                        "\n" +
                        "\uD83D\uDCC5 Дата: %s  \n" +
                        "⏰ Время: %s  \n" +
                        "✨ Услуга: %s  \n" +
                        "\n" +
                        "Мастер изменил время твоей записи.  \n" +
                        "Если новое время тебе не подходит — напиши мастеру в личные сообщения: %s", bookingService.formatTheDate(booking.getBookingDate()), booking.getBookingTime(), booking.getBookingType().getLabel(), properties.getAdminLink()))
                .build();

        bot.sendNewMessage(message);
    }
}