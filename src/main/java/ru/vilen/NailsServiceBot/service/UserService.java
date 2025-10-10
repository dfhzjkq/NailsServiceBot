package ru.vilen.NailsServiceBot.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.vilen.NailsServiceBot.entity.User;
import ru.vilen.NailsServiceBot.entity.UserState;
import ru.vilen.NailsServiceBot.repository.UserRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
public class UserService {

    UserRepository userRepository;

    public User getOrCreateUser(Long chatId) {
        return userRepository.findById(chatId)
                .orElseGet(() -> {
                    User newUser = new User(chatId, null, null, null, null, UserState.WAITING_NAME);
                    return userRepository.save(newUser);
                });
    }

    public void saveName(Long chatId, String name) {
        User user = getOrCreateUser(chatId);
        user.setUserName(name);
        user.setUserState(UserState.WAITING_PHONE);
        userRepository.save(user);
    }

    public void savePhone(Long chatId, String phone) {
        User user = getOrCreateUser(chatId);
        user.setPhoneNumber(phone);
        user.setUserState(UserState.REGISTERED);
        userRepository.save(user);
    }

    public void saveBookingDate(Long chatId, String date) {
        User user = getOrCreateUser(chatId);
        user.setBookingDate(date);
        user.setUserState(UserState.WAITING_BOOK);
        userRepository.save(user);
    }

    public void saveBookingTime(Long chatId, String time) {
        User user = getOrCreateUser(chatId);
        user.setBookingTime(time);
        user.setUserState(UserState.REGISTERED);
        userRepository.save(user);
    }

    public void deleteBooking(Long chatId) {
        User user = getOrCreateUser(chatId);
        user.setBookingDate(null);
        user.setBookingTime(null);
        userRepository.save(user);
    }

    public void updateName(Long chatId, String name) {
        User user = getOrCreateUser(chatId);
        user.setUserName(name);
        user.setUserState(UserState.REGISTERED);
        userRepository.save(user);
    }

    public void updatePhone(Long chatId, String phone) {
        User user = getOrCreateUser(chatId);
        user.setPhoneNumber(phone);
        user.setUserState(UserState.REGISTERED);
        userRepository.save(user);
    }

    public boolean isRegistered(Long chatId) {
        User user = getOrCreateUser(chatId);
        return user.getUserState() == UserState.REGISTERED ||
               user.getUserState() == UserState.WAITING_BOOK;
    }
}
