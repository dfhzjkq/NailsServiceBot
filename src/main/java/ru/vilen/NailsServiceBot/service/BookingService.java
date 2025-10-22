    package ru.vilen.NailsServiceBot.service;

    import jakarta.transaction.Transactional;
    import lombok.AccessLevel;
    import lombok.RequiredArgsConstructor;
    import lombok.experimental.FieldDefaults;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.stereotype.Service;
    import ru.vilen.NailsServiceBot.entity.Book;
    import ru.vilen.NailsServiceBot.entity.BookingStatus;
    import ru.vilen.NailsServiceBot.entity.User;
    import ru.vilen.NailsServiceBot.repository.BookingRepository;
    import ru.vilen.NailsServiceBot.repository.UserRepository;

    import java.time.LocalDate;
    import java.time.LocalTime;
    import java.util.List;

    @Slf4j
    @Service
    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @Transactional
    public class BookingService {

        BookingRepository bookingRepository;
        UserRepository userRepository;

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

            if (bookingRepository.existsByBookingDateAndBookingTime(booking.getBookingDate(), time)) {
                log.warn("Время [{}] уже занято", time);
                throw new IllegalStateException("Это время уже занято!");
            }

            booking.setBookingTime(time);
            booking.setStatus(BookingStatus.PENDING);
            bookingRepository.save(booking);

            log.info("Создана полная запись chatId[{}], date[{}], time[{}]", chatId, booking.getBookingDate(), time);
        }

        public Book getActiveBooking(Long chatId) {
            return bookingRepository.findFirstByUserChatIdAndStatusIn(
                    chatId,
                    List.of(BookingStatus.WAITING_TIME, BookingStatus.PENDING, BookingStatus.CONFIRMED)
            ).orElseThrow(() -> new IllegalStateException("У вас нет активных записей"));
        }

        public void cancelBooking(Long chatId) {
            log.debug("Отмена записи пользователя chatId[{}]", chatId);
            bookingRepository.findFirstByUserChatIdAndStatus(chatId, BookingStatus.PENDING).ifPresent(booking -> {
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
            });
        }

        public void clearUnfinishedBooking(Long chatId) {
            bookingRepository.findFirstByUserChatIdAndStatus(chatId, BookingStatus.WAITING_TIME)
                    .ifPresent(booking -> {
                        bookingRepository.delete(booking);
                        log.info("Удалена незавершённая запись chatId[{}]", chatId);
                    });
        }
    }
