package ru.vilen.NailsServiceBot.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingService {

    Map<Long, List<LocalDate>>  bookings = new HashMap<>();

    public List<LocalDate> getBookings(Long chatId) {
        return bookings.getOrDefault(chatId, new ArrayList<>());
    }

    public void addBooking(Long chatId, LocalDate date) {
        bookings.computeIfAbsent(chatId, id -> new ArrayList<>()).add(date);
    }

    public void cancelBooking(Long chatId, LocalDate date) {}
}
