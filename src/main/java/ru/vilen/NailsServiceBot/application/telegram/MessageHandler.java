package ru.vilen.NailsServiceBot.application.telegram;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.vilen.NailsServiceBot.entity.User;
import ru.vilen.NailsServiceBot.entity.UserState;
import ru.vilen.NailsServiceBot.service.UserService;
import ru.vilen.NailsServiceBot.utils.UserKeyboardUtils;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageHandler {

    TelegramBot bot;
    UserService userService;

    public void handle(Update update) {
        Long chatId = update.getMessage().getChatId();
        User user = userService.getOrCreateUser(chatId);
        UserState userState = user.getUserState();
        String text = update.getMessage().getText().trim();

        switch (userState) {
            case WAITING_NAME -> {
                userService.saveName(chatId, update.getMessage().getText());

                SendMessage askPhone = SendMessage.builder()
                        .chatId(chatId)
                        .text("\uD83D\uDCDE Отлично!  \n" +
                                "Теперь напиши, пожалуйста, свой номер телефона \uD83D\uDC85  \n" +
                                "(Просто отправь цифры, например: 89991234567)")
                        .build();
                bot.sendNewMessage(askPhone);
            }
            case WAITING_PHONE -> {
                userService.savePhone(chatId, update.getMessage().getText());

                SendMessage successMessage = SendMessage.builder()
                        .chatId(chatId)
                        .text("\uD83D\uDC96 Готово! Регистрация прошла успешно \uD83C\uDF38  \n" +
                                "Теперь ты можешь записаться на маникюр \uD83D\uDC85 ")
                        .replyMarkup(UserKeyboardUtils.buildHomeInlineKeyboard())
                        .build();
                bot.sendNewMessage(successMessage);
            }
            case WAITING_BOOK -> {
                userService.saveBookingTime(chatId, text);
                User u = userService.getOrCreateUser(chatId);

                SendMessage confirm = SendMessage.builder()
                        .chatId(chatId)
                        .text("✅ Запись создана!\n" +
                                "Дата: " + user.getBookingDate() + "\n" +
                                "Время: " + u.getBookingTime() + "\n" +
                                "Мастер свяжется с тобой в течение 10 минут 💅")
                        .replyMarkup(UserKeyboardUtils.buildHomeInlineKeyboard())
                        .build();
                bot.sendNewMessage(confirm);
            }
            case WAITING_NEW_NAME -> {
                userService.updateName(chatId, update.getMessage().getText());

                SendMessage message = SendMessage.builder()
                        .chatId(chatId)
                        .text("✅ Имя успешно обновлено!\nВозвращайся в главное меню \uD83D\uDC47")
                        .replyMarkup(UserKeyboardUtils.buildHomeInlineKeyboard())
                        .build();
                bot.sendNewMessage(message);
            }
            case WAITING_NEW_PHONE -> {
                userService.updatePhone(chatId, update.getMessage().getText());

                SendMessage message = SendMessage.builder()
                        .chatId(chatId)
                        .text("✅ Номер успешно обновлен!\nВозвращайся в главное меню \uD83D\uDC47")
                        .replyMarkup(UserKeyboardUtils.buildHomeInlineKeyboard())
                        .build();
                bot.sendNewMessage(message);
            } default -> {
                SendMessage successMessage = SendMessage.builder()
                        .chatId(chatId)
                        .text("Ой, кажется, такой команды нет \uD83D\uDCAC  \n" +
                                "Переходи в главное меню, чтобы выбрать нужное действие ⬇\uFE0F")
                        .replyMarkup(UserKeyboardUtils.buildHomeInlineKeyboard())
                        .build();
                bot.sendNewMessage(successMessage);
            }
        }
    }
}
