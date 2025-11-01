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
    import ru.vilen.NailsServiceBot.entity.Book;
    import ru.vilen.NailsServiceBot.entity.BookingStatus;
    import ru.vilen.NailsServiceBot.entity.User;
    import ru.vilen.NailsServiceBot.repository.BookingRepository;
    import ru.vilen.NailsServiceBot.repository.UserRepository;
    import ru.vilen.NailsServiceBot.utils.AdminKeyboardUtils;

    import java.time.LocalDate;
    import java.time.LocalTime;
    import java.time.format.DateTimeFormatter;
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

            User user = userRepository.findById(chatId)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден!"));

            bookingRepository.findFirstByUserChatIdAndStatus(chatId, BookingStatus.WAITING_TIME)
                    .ifPresent(bookingRepository::delete);

            Book booking = new Book();
            booking.setUser(user);
            booking.setBookingDate(date);
            booking.setStatus(BookingStatus.WAITING_TIME);

            bookingRepository.save(booking);
            log.info("Создана запись chatId[{}], date[{}]. Ожидание выбора времени", chatId, date);
        }

        public void saveBookingTime(Long chatId, LocalTime time) {
            Book booking = bookingRepository.findFirstByUserChatIdAndStatus(chatId, BookingStatus.WAITING_TIME)
                    .orElseThrow(() -> new IllegalStateException("Сначала нужно выбрать дату"));

            Book existingBooking = bookingRepository.findFirstByUserChatIdAndStatusIn(
                    chatId, List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
            ).orElse(null);

            if (existingBooking != null) {
                deleteBookingById(existingBooking.getId());
                bot.sendNewMessage(SendMessage.builder()
                        .chatId(chatId)
                        .text("""
                        ⚠️ Твоя предыдущая запись была отменена, чтобы освободить место для новой.\n
                        📅 Не переживай! Я уже сохранил новую дату и время 💖
                        """)
                        .build());
            }

            if (bookingRepository.existsByBookingDateAndBookingTime(booking.getBookingDate(), time)) {
                log.warn("Время [{}] уже занято", time);
                throw new IllegalStateException("Это время уже занято!");
            }

            booking.setBookingTime(time);
            booking.setStatus(BookingStatus.PENDING);
            bookingRepository.save(booking);

            log.info("Создана полная запись chatId[{}], date[{}], time[{}]", chatId, booking.getBookingDate(), time);
            sendBookingNotificationToAdmin(booking);
        }

        public Book getActiveBooking(Long chatId) {
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
            });
        }

        public void clearUnfinishedBooking(Long chatId) {
            bookingRepository.findFirstByUserChatIdAndStatus(chatId, BookingStatus.WAITING_TIME)
                    .ifPresent(booking -> {
                        bookingRepository.delete(booking);
                        log.info("Удалена незавершённая запись chatId[{}]", chatId);
                    });
        }

        public List<Book> getAllBookings() {
            return bookingRepository.findAll(Sort.by("bookingDate").ascending().and(Sort.by("bookingTime").ascending()));
        }

        private void sendBookingNotificationToAdmin(Book booking) {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String formattedDate = booking.getBookingDate().format(dateFormatter);

            String userName = booking.getUser().getUserName();
            Long chatId = booking.getUser().getChatId();



            String userLink;
            if (userName != null && !userName.isBlank()) {
                userLink = String.format("<a href=\"https://t.me/%s\">@%s</a>", userName, userName);
            } else {
                userLink = String.format("<a href=\"tg://user?id=%d\">Профиль пользователя</a>", chatId);
            }
            String text = String.format("""
                Новая запись!
                Клиент: %s
                Телефон: %s
                Запись: %s (%s)
                @%s
                """,
                    userName,
                    booking.getUser().getPhoneNumber(),
                    booking.getBookingTime(),
                    formattedDate,
                    booking.getUser().getUserLink()
            );

            SendMessage message = SendMessage.builder()
                    .chatId(adminChatId)
                    .text(text)
                    .replyMarkup(AdminKeyboardUtils.buildDecisionInlineKeyboard(booking.getId()))
                    .build();

            bot.sendNewMessage(message);
        }

        public void confirmBooking(Long bookingId) {
            bookingRepository.findById(bookingId).ifPresent(booking -> {
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);

                bot.sendNewMessage(SendMessage.builder()
                        .chatId(booking.getUser().getChatId())
                        .text("✅ Ваша запись подтверждена!\n📅 " + booking.getBookingDate() + " ⏰ " + booking.getBookingTime())
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
    }
