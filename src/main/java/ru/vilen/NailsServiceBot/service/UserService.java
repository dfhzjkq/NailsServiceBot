package ru.vilen.NailsServiceBot.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.vilen.NailsServiceBot.model.User;
import ru.vilen.NailsServiceBot.model.UserState;

import java.util.HashMap;
import java.util.Map;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserService {

    Map<Long, User> users = new HashMap<>();

    public User getOrCreateUser(Long chatId) {
        return users.computeIfAbsent(chatId,
                id -> new User(chatId, null, null, UserState.WAITING_NAME));
    }

    public void saveName(Long chatId, String name) {
        User user = getOrCreateUser(chatId);
        user.setUserName(name);
        user.setUserState(UserState.WAITING_PHONE);
    }

    public void savePhone(Long chatId, String phone) {
        User user = getOrCreateUser(chatId);
        user.setPhoneNumber(phone);
        user.setUserState(UserState.REGISTERED);
    }

    public boolean isRegistered(Long chatId) {
        User user = getOrCreateUser(chatId);
        return user.getUserState() == UserState.REGISTERED;
    }
}
