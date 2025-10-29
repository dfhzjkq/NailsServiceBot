package ru.vilen.NailsServiceBot.application.telegram.callback.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.vilen.NailsServiceBot.application.telegram.TelegramBot;
import ru.vilen.NailsServiceBot.application.telegram.callback.Callback;
import ru.vilen.NailsServiceBot.application.telegram.callback.CallbackType;
import ru.vilen.NailsServiceBot.entity.User;
import ru.vilen.NailsServiceBot.entity.UserStatus;
import ru.vilen.NailsServiceBot.repository.UserRepository;
import ru.vilen.NailsServiceBot.service.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChangePhoneCallback implements Callback {

    TelegramBot bot;
    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    public void apply(Update update) {
        Long userId = update.getCallbackQuery().getFrom().getId();
        String userName = update.getCallbackQuery().getFrom().getUserName();
        log.info("[{}] Callback {} от пользователя {} [id{}]",
                update.getUpdateId(),
                getType(),
                userName,
                userId);

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        User user = userService.getOrCreateUser(chatId);
        user.setUserState(UserStatus.WAITING_NEW_PHONE);
        userRepository.save(user);

        log.info("User state пользователя [{}]",  user.getUserState());

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("\uD83D\uDCF2 Пожалуйста, введи новый номер телефона")
                .build();
        bot.sendNewMessage(message);
    }

    @Override
    public CallbackType getType() {
        return CallbackType.CHANGE_PHONE;
    }
}
