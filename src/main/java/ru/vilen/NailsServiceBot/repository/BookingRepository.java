package ru.vilen.NailsServiceBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.vilen.NailsServiceBot.entity.Booking;
import ru.vilen.NailsServiceBot.entity.BookingStatus;
import ru.vilen.NailsServiceBot.entity.User;
import ru.vilen.NailsServiceBot.entity.UserStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByUser(User user);

    List<Booking> findAllByBookingDate(LocalDate date);

    boolean existsByBookingDateAndBookingTime(LocalDate date,  LocalTime time);

    Optional<Booking> findFirstByUserChatIdAndStatus(Long chatId, BookingStatus status);

    Optional<Booking> findFirstByUserChatIdAndStatusIn(Long chatId, List<BookingStatus> statuses);

    Optional<Booking> findFirstByUserChatId(Long chatId);

    List<Booking> findAllByBookingDateAndBookingTimeIsNotNull(LocalDate date);

    Optional<Booking> findFirstByUser_UserState(UserStatus userStatus);
}
