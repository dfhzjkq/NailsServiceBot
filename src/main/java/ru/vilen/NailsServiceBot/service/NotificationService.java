package ru.vilen.NailsServiceBot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.vilen.NailsServiceBot.application.telegram.TelegramBot;
import ru.vilen.NailsServiceBot.config.TelegramBotProperties;
import ru.vilen.NailsServiceBot.entity.Booking;
import ru.vilen.NailsServiceBot.entity.BookingStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final TelegramBotProperties properties;
    private final BookingService bookingService;
    private final UserService userService;
    private final TelegramBot bot;

    @Scheduled(fixedRate = 30000)
    public void sendReminders() {

        List<Booking> bookings = bookingService.getAllBookings();
        LocalDateTime now = LocalDateTime.now();

        for (Booking booking : bookings) {

            if (booking.getBookingTime() == null) continue;
            if (booking.getStatus() != BookingStatus.CONFIRMED) continue;

            LocalDateTime bookingDateTime = LocalDateTime.of(
                    booking.getBookingDate(),
                    booking.getBookingTime()
            );

            Duration diff = Duration.between(now, bookingDateTime);
            long seconds = diff.getSeconds();

            if (!booking.getReminderSent()
                    && seconds <= 3600
                    && seconds >= 3510) {

                sendReminderToClient(booking);
                sendReminderToAdmin(booking);

                booking.setReminderSent(true);
                bookingService.saveBooking(booking);
            }
        }
    }

    private void sendReminderToClient(Booking booking) {
        bot.sendNewMessage(SendMessage.builder()
                .chatId(booking.getUser().getChatId())
                .text(String.format(
                        "⏰ Напоминание!\n" +
                                "Твоя запись начнётся через 1 час.\n\n" +
                                "📅 %s\n" +
                                "⏰ %s\n" +
                                "%s\n\n" +
                                "Если нужно что-то уточнить — пиши мастеру: %s",
                        bookingService.formatTheDate(booking.getBookingDate()),
                        booking.getBookingTime(),
                        booking.getBookingType().getLabel(),
                        properties.getAdminLink()
                ))
                .build());
    }

    private void sendReminderToAdmin(Booking booking) {
        bot.sendNewMessage(SendMessage.builder()
                .chatId(userService.getAdminChatId())
                .text(String.format(
                        "⏰ Напоминание!\n" +
                                "Через час клиент придёт на запись.\n\n" +
                                "Клиент: %s\n" +
                                "📅 %s\n" +
                                "⏰ %s\n" +
                                "%s",
                        booking.getUser().getUserName(),
                        bookingService.formatTheDate(booking.getBookingDate()),
                        booking.getBookingTime(),
                        booking.getBookingType().getLabel()
                ))
                .build());
    }
}
