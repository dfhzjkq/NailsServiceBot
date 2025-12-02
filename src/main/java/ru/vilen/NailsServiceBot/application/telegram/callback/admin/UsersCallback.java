package ru.vilen.NailsServiceBot.application.telegram.callback.admin;

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
import ru.vilen.NailsServiceBot.entity.UserRole;
import ru.vilen.NailsServiceBot.service.UserService;
import ru.vilen.NailsServiceBot.utils.AdminKeyboardUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UsersCallback implements Callback {
    TelegramBot bot;
    UserService userService;

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

        List<User> users = userService.getAllUsers();

        if (users.isEmpty()) {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text("Пока нет пользователей!")
                    .replyMarkup(AdminKeyboardUtils.buildScheduleInlineKeyboard())
                    .build();

            bot.sendNewMessage(message);
            return;
        }

        StringBuilder sb = new StringBuilder("Все пользователи:\n\n");

        for (User user : users) {
            if (user.getRole() != UserRole.ADMIN) {
                String line = String.format("""
                Имя: %s
                Телефон: <code>%s</code>
                Id: <code>%d</code>
                Ссылка: @%s\n
                """,
                        user.getUserName(),
                        user.getPhoneNumber(),
                        user.getChatId(),
                        user.getUserLink()
                );

                sb.append(line);
            }
        }

        SendMessage bookingsMessage = SendMessage.builder()
                .chatId(chatId)
                .text(sb.toString())
                .replyMarkup(AdminKeyboardUtils.buildUsersInlineKeyboard())
                .parseMode("HTML")
                .build();

        bot.sendNewMessage(bookingsMessage);
    }

    @Override
    public CallbackType getType() {
        return CallbackType.USERS;
    }
}
