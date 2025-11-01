package ru.vilen.NailsServiceBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.vilen.NailsServiceBot.entity.Book;
import ru.vilen.NailsServiceBot.entity.BookingStatus;
import ru.vilen.NailsServiceBot.entity.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Book, Long> {

    List<Book> findAllByUser(User user);

    List<Book> findAllByBookingDate(LocalDate date);

    boolean existsByBookingDateAndBookingTime(LocalDate date,  LocalTime time);

    Optional<Book> findFirstByUserChatIdAndStatus(Long chatId, BookingStatus status);

    Optional<Book> findFirstByUserChatIdAndStatusIn(Long chatId, List<BookingStatus> statuses);

    Optional<Book> findFirstByUserChatId(Long chatId);
}
