package ru.vilen.NailsServiceBot.application.telegram;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.vilen.NailsServiceBot.model.User;
import ru.vilen.NailsServiceBot.service.UserService;
import ru.vilen.NailsServiceBot.utils.KeyboardUtils;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RegisterHandler {

    TelegramBot bot;
    UserService userService;

    public void handle(Update update) {
        Long chatId = update.getMessage().getChatId();
        User user = userService.getOrCreateUser(chatId);

        switch (user.getUserState()) {
            case WAITING_NAME -> {
                userService.saveName(chatId, update.getMessage().getText());

                SendMessage askPhone = SendMessage.builder()
                        .chatId(chatId)
                        .text("Напиши свой номер телефона")
                        .build();
                bot.sendNewMessage(askPhone);
            }

            case WAITING_PHONE -> {
                userService.savePhone(chatId, update.getMessage().getText());

                SendMessage successMessage = SendMessage.builder()
                        .chatId(chatId)
                        .text("Регистрация прошла успешно!")
                        .replyMarkup(KeyboardUtils.buildHomeInlineKeyboard())
                        .build();
                bot.sendNewMessage(successMessage);
            } default -> {
                SendMessage successMessage = SendMessage.builder()
                        .chatId(chatId)
                        .text("В боте нет такой команды.\nМожешь перейти в главное меню по кнопке ниже")
                        .replyMarkup(KeyboardUtils.buildHomeInlineKeyboard())
                        .build();
                bot.sendNewMessage(successMessage);
            }
        }
    }
}
