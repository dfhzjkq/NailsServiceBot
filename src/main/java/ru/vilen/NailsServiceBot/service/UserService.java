package ru.vilen.NailsServiceBot.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.vilen.NailsServiceBot.entity.User;
import ru.vilen.NailsServiceBot.entity.UserRole;
import ru.vilen.NailsServiceBot.entity.UserStatus;
import ru.vilen.NailsServiceBot.repository.UserRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
@Transactional
public class UserService {

    UserRepository userRepository;

    public User getOrCreateUser(Long chatId) {
        return userRepository.findById(chatId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setChatId(chatId);
                    newUser.setUserState(UserStatus.WAITING_NAME);
                    newUser.setRole(UserRole.USER);
                    return userRepository.save(newUser);
                });
    }

    public void saveName(Long chatId, String name) {
        User user = getOrCreateUser(chatId);
        user.setUserName(name);
        user.setUserState(UserStatus.WAITING_PHONE);
        userRepository.save(user);
    }

    public void savePhone(Long chatId, String phone) {
        User user = getOrCreateUser(chatId);
        user.setPhoneNumber(phone);
        user.setUserState(UserStatus.REGISTERED);
        userRepository.save(user);
    }

    public void updateName(Long chatId, String name) {
        User user = getOrCreateUser(chatId);
        user.setUserName(name);
        user.setUserState(UserStatus.REGISTERED);
        userRepository.save(user);
    }

    public void updatePhone(Long chatId, String phone) {
        User user = getOrCreateUser(chatId);
        user.setPhoneNumber(phone);
        user.setUserState(UserStatus.REGISTERED);
        userRepository.save(user);
    }

    public boolean isAdmin(Long chatId) {
        return userRepository.findById(chatId)
                .map(user -> user.getRole() == UserRole.ADMIN)
                .orElse(false);
    }
}
