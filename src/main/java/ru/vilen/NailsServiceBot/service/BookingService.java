package ru.vilen.NailsServiceBot.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.vilen.NailsServiceBot.application.telegram.TelegramBot;
import ru.vilen.NailsServiceBot.entity.*;
import ru.vilen.NailsServiceBot.repository.BookingRepository;
import ru.vilen.NailsServiceBot.repository.UserRepository;
import ru.vilen.NailsServiceBot.utils.AdminKeyboardUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional
public class BookingService {

    @Value("${bot.admin}")
    Long adminChatId;
    final BookingRepository bookingRepository;
    final UserRepository userRepository;
    final TelegramBot bot;

    public void saveBookingDate(Long chatId, LocalDate date) {
        log.debug("Создание новой записи для пользователя chatId[{}], date[{}]", chatId, date);
//
//        User user = userRepository.findById(chatId)
//                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден!"));
//
//        bookingRepository.findFirstByUserChatIdAndStatus(chatId, BookingStatus.WAITING_TIME)
//                .ifPresent(bookingRepository::delete);

        Booking booking = bookingRepository.findFirstByUserChatIdAndStatus(chatId, BookingStatus.WAITING_DATE)
                .orElseThrow(() -> new IllegalStateException("Сначала нужно выбрать тип услуги"));

        booking.setBookingDate(date);
        booking.setStatus(BookingStatus.WAITING_TIME);

        bookingRepository.save(booking);
        log.info("Создана запись chatId[{}], date[{}]. Ожидание выбора времени", chatId, date);
    }

    public void saveBookingType(Long chatId, BookingType bookingType) {
        User user = userRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден!"));

        bookingRepository.findFirstByUserChatIdAndStatus(chatId, BookingStatus.WAITING_TIME)
                .ifPresent(bookingRepository::delete);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setBookingType(bookingType);
        booking.setStatus(BookingStatus.WAITING_DATE);

        bookingRepository.save(booking);
    }

    public void saveBookingTime(Long chatId, LocalTime time) {

        Booking booking = bookingRepository.findFirstByUserChatIdAndStatus(chatId, BookingStatus.WAITING_TIME)
                .orElseThrow(() -> new IllegalStateException("Сначала нужно выбрать дату"));

        LocalDate date = booking.getBookingDate();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

    /* ======================================================
       1. Проверка: нельзя записаться на прошедшее время
       ====================================================== */
        if (date.equals(today)) {

            LocalTime minAllowed = now.plusMinutes(10);

            if (time.isBefore(minAllowed)) {

                bot.sendNewMessage(SendMessage.builder()
                        .chatId(chatId)
                        .text("""
                            ⏰ Это время уже недоступно.

                            Можно записаться минимум через 10 минут от текущего момента.
                            """)
                        .build());
                return;
            }
        }

    /* ======================================================
       2. Проверка: время входит в доступный интервал
       ====================================================== */
        List<String> intervals = getAvailableIntervals(chatId, date);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

        boolean valid = intervals.stream().anyMatch(interval -> {
            if (!interval.contains("—")) {
                // одиночное время
                return interval.equals(time.format(fmt));
            } else {
                // интервал "HH:mm — HH:mm"
                String[] parts = interval.split("—");
                LocalTime start = LocalTime.parse(parts[0].trim());
                LocalTime end = LocalTime.parse(parts[1].trim());
                return !time.isBefore(start) && !time.isAfter(end);
            }
        });

        if (!valid) {
            bot.sendNewMessage(SendMessage.builder()
                    .chatId(chatId)
                    .text("""
                        ⏰ Это время недоступно.

                        Пожалуйста, выбери время из предложенных интервалов.
                        """)
                    .build());
            return;
        }

    /* ======================================================
       3. Если есть старая запись — отменяем
       ====================================================== */
        Booking existingBooking = bookingRepository.findFirstByUserChatIdAndStatusIn(
                chatId, List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        ).orElse(null);

        if (existingBooking != null) {

            deleteBookingById(existingBooking.getId());

            bot.sendNewMessage(SendMessage.builder()
                    .chatId(chatId)
                    .text("""
                        ⚠️ Твоя предыдущая запись была отменена, чтобы освободить место для новой.

                        📅 Не переживай! Я уже сохранил новую дату и время 💖
                        """)
                    .build());

            bot.sendNewMessage(SendMessage.builder()
                    .chatId(adminChatId)
                    .text(String.format("Клиент %s отменил запись на %s (%s)",
                            existingBooking.getUser().getUserName(),
                            existingBooking.getBookingTime(),
                            formatTheDate(existingBooking.getBookingDate())))
                    .build());
        }

    /* ======================================================
       4. Проверка пересечений по длительности услуги
       ====================================================== */
        Duration newDur = booking.getBookingType().getDuration();
        LocalTime newStart = time;
        LocalTime newEnd = time.plus(newDur);

        List<Booking> existing = bookingRepository.findAllByBookingDate(date)
                .stream()
                .filter(b -> b.getBookingTime() != null)
                .filter(b -> List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
                        .contains(b.getStatus()))
                .toList();

        for (Booking b : existing) {
            Duration dur = b.getBookingType().getDuration();
            LocalTime start = b.getBookingTime();
            LocalTime end = start.plus(dur);

            boolean overlap = newStart.isBefore(end) && newEnd.isAfter(start);

            if (overlap) {
                bot.sendNewMessage(SendMessage.builder()
                        .chatId(chatId)
                        .text("⛔ Это время пересекается с другой записью. Выбери другое время.")
                        .build());
                return;
            }
        }

    /* ======================================================
       5. Сохраняем запись
       ====================================================== */
        booking.setBookingTime(time);
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        log.info("Создана запись chatId[{}], date[{}], time[{}]", chatId, date, time);

        sendBookingNotificationToAdmin(booking);
    }

    public Booking getActiveBooking(Long chatId) {
        return bookingRepository.findFirstByUserChatIdAndStatusIn(
                chatId,
                List.of(BookingStatus.WAITING_TIME, BookingStatus.PENDING, BookingStatus.CONFIRMED)
        ).orElseThrow(() -> new IllegalStateException("У вас нет активных записей"));
    }

    public void cancelBooking(Long chatId) {
        log.debug("Отмена записи пользователя chatId[{}]", chatId);
        bookingRepository.findFirstByUserChatId(chatId).ifPresent(booking -> {
            booking.setStatus(BookingStatus.CANCELLED);
            deleteBookingById(booking.getId());
//                bookingRepository.save(booking);

            SendMessage message = SendMessage.builder()
                    .chatId(adminChatId)
                    .text(String.format("Клиент %s отменил запись на %s(%s)",
                            booking.getUser().getUserName(),
                            booking.getBookingTime(),
                            formatTheDate(booking.getBookingDate())))
                    .build();

            bot.sendNewMessage(message);
        });


    }

    public void clearUnfinishedBooking(Long chatId) {
        bookingRepository.findFirstByUserChatIdAndStatus(chatId, BookingStatus.WAITING_TIME)
                .ifPresent(booking -> {
                    bookingRepository.delete(booking);
                    log.info("Удалена незавершённая запись chatId[{}]", chatId);
                });
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll(Sort.by("bookingDate").ascending().and(Sort.by("bookingTime").ascending()));
    }

    private void sendBookingNotificationToAdmin(Booking booking) {
        LocalDate date = booking.getBookingDate();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Берём только подтверждённые записи с временем
        List<Booking> confirmedBookings = bookingRepository.findAllByBookingDate(date)
                .stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .filter(b -> b.getBookingTime() != null)
                .sorted(Comparator.comparing(Booking::getBookingTime))
                .toList();

        StringBuilder schedule = new StringBuilder("📅 Подтверждённые записи на " + date.format(dateFormatter) + ":\n\n");

        if (confirmedBookings.isEmpty()) {
            schedule.append("Нет подтверждённых записей.\n\n");
        } else {
            for (Booking b : confirmedBookings) {

                Duration duration = b.getBookingType().getDuration(); // теперь Duration
                String procedure = b.getBookingType().getLabel();

                LocalTime startTime = b.getBookingTime();
                LocalTime endTime = startTime.plus(duration); // <-- ключевое изменение

                String start = startTime.format(timeFormatter);
                String end = endTime.format(timeFormatter);

                schedule.append(String.format("""
                    <blockquote>%s</blockquote>
                    <b>%s — %s</b>
                    Клиент: %s
                    Телефон: <code>%s</code>
                    Услуга: %s
                    Ссылка: @%s
        
                    """,
                    b.getBookingDate().format(dateFormatter),
                    start,
                    end,
                    b.getUser().getUserName(),
                    b.getUser().getPhoneNumber(),
                    procedure,
                    b.getUser().getUserLink()
                ));
            }
        }

        // Отправляем расписание
        bot.sendNewMessage(SendMessage.builder()
                .chatId(adminChatId)
                .text(schedule.toString())
                .parseMode("HTML")
                .build());


        // Отдельное сообщение — новая запись (PENDING)
        String text = String.format("""
        <b>Новая запись!</b>
        Клиент: %s
        Телефон: <code>%s</code>
        Услуга: %s
        Время: %s (%s)
        Ссылка: @%s
        """,
                booking.getUser().getUserName(),
                booking.getUser().getPhoneNumber(),
                booking.getBookingType().getLabel(),
                booking.getBookingTime().format(timeFormatter),
                date.format(dateFormatter),
                booking.getUser().getUserLink()
        );

        bot.sendNewMessage(SendMessage.builder()
                .chatId(adminChatId)
                .text(text)
                .replyMarkup(AdminKeyboardUtils.buildDecisionInlineKeyboard(booking.getId()))
                .parseMode("HTML")
                .build());
    }

    public void confirmBooking(Long bookingId) {
        bookingRepository.findById(bookingId).ifPresent(booking -> {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            bot.sendNewMessage(SendMessage.builder()
                    .chatId(booking.getUser().getChatId())
                    .text("✅ Ваша запись подтверждена!\n📅 " + formatTheDate(booking.getBookingDate()) + " ⏰ " + booking.getBookingTime())
                    .build());
        });
    }

    public void rejectBooking(Long bookingId) {
        bookingRepository.findById(bookingId).ifPresent(booking -> {
            booking.setStatus(BookingStatus.REJECTED);
            deleteBookingById(bookingId);
//                bookingRepository.save(booking);

            bot.sendNewMessage(SendMessage.builder()
                    .chatId(booking.getUser().getChatId())
                    .text("⚠️ К сожалению, ваша запись отклонена.")
                    .build());
        });
    }

    public void deleteBookingById(Long id) {
        if (bookingRepository.existsById(id)) {
            bookingRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public List<String> getAvailableIntervals(Long chatId, LocalDate date) {

        Booking waiting = bookingRepository.findFirstByUserChatIdAndStatus(chatId, BookingStatus.WAITING_TIME)
                .orElse(null);

        Duration required = waiting != null && waiting.getBookingType() != null
                ? waiting.getBookingType().getDuration()
                : Duration.ZERO;

        List<Booking> bookings = bookingRepository
                .findAllByBookingDateAndBookingTimeIsNotNull(date).stream()
                .sorted(Comparator.comparing(Booking::getBookingTime))
                .toList();

        LocalTime dayStart = LocalTime.of(9, 0);
        LocalTime dayEnd = LocalTime.of(22, 0);

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

// если дата сегодняшняя — начало рабочего дня смещается вперёд
        if (date.equals(today)) {
            LocalTime minStart = now.plusMinutes(10);

            if (minStart.isAfter(dayStart)) {
                dayStart = minStart;
            }

            // если уже позже конца рабочего дня — свободных слотов нет
            if (dayStart.isAfter(dayEnd)) {
                return List.of();
            }
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

        // структура для хранения занятости
        record Interval(LocalTime start, LocalTime end) {}

        /* ---------------- ЗАНЯТЫЕ ИНТЕРВАЛЫ ---------------- */
        List<Interval> busy = new ArrayList<>();
        for (Booking b : bookings) {
            Duration dur = b.getBookingType().getDuration(); // duration теперь в минутах
            busy.add(new Interval(
                    b.getBookingTime(),
                    b.getBookingTime().plus(dur)
            ));
        }

        /* ---------------- СВОБОДНЫЕ ОКНА ---------------- */
        List<Interval> free = new ArrayList<>();

        if (busy.isEmpty()) {
            free.add(new Interval(dayStart, dayEnd));
        } else {
            // окно до первой записи
            if (dayStart.isBefore(busy.get(0).start)) {
                free.add(new Interval(dayStart, busy.get(0).start));
            }

            // промежутки между
            for (int i = 0; i < busy.size() - 1; i++) {
                LocalTime gapStart = busy.get(i).end;
                LocalTime gapEnd = busy.get(i + 1).start;
                if (gapStart.isBefore(gapEnd)) {
                    free.add(new Interval(gapStart, gapEnd));
                }
            }

            // окно после последней записи
            if (busy.get(busy.size() - 1).end.isBefore(dayEnd)) {
                free.add(new Interval(busy.get(busy.size() - 1).end, dayEnd));
            }
        }

        /* ---------------- ПРЕОБРАЗУЕМ СВОБОДНЫЕ ОКНА В ДОСТУПНЫЕ СТАРТОВЫЕ ВРЕМЕНА ---------------- */
        List<String> intervals = new ArrayList<>();

        for (Interval gap : free) {

            // позднейшее возможное начало
            LocalTime latestStart = gap.end.minus(required);

            if (!gap.start.isAfter(latestStart)) {
                String s = gap.start.format(fmt);
                String e = latestStart.format(fmt);

                if (s.equals(e)) {
                    intervals.add(s);        // один вариант
                } else {
                    intervals.add(s + " — " + e);
                }
            }
        }

        return intervals;
    }

    public String formatTheDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Запись не найдена"));
    }

    public void saveBooking(Booking booking) {
        bookingRepository.save(booking);
    }

    public Booking getBookingByUserStatus(UserStatus userStatus) {
        return bookingRepository.findFirstByUser_UserState(userStatus)
                .orElseThrow(() -> new IllegalStateException("Запись не найдена"));
    }

}