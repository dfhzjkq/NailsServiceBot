package ru.vilen.NailsServiceBot.application.telegram.callback.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.vilen.NailsServiceBot.application.telegram.TelegramBot;
import ru.vilen.NailsServiceBot.application.telegram.callback.Callback;
import ru.vilen.NailsServiceBot.application.telegram.callback.CallbackType;
import ru.vilen.NailsServiceBot.entity.User;
import ru.vilen.NailsServiceBot.service.UserService;
import ru.vilen.NailsServiceBot.utils.AdminKeyboardUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BanCallback implements Callback {

    TelegramBot bot;
    UserService userService;

    @Override
    public CallbackType getType() {
        return CallbackType.BAN;
    }

    @Override
    public void apply(Update update) {

        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        log.debug("[DELETE_BOOKING] data={}", data);

        if (data.startsWith("SELECT_USER_")) {
            handleSelectUser(data, chatId);
            return;
        }

        if (data.startsWith("BAN_USER_")) {
            handleBanUser(data, chatId);
            return;
        }

        if (data.startsWith("UNBAN_USER_")) {
            handleUnbanUser(data, chatId);
            return;
        }

        showUsers(chatId);
    }

    private void showUsers(Long chatId) {
        List<User> users = userService.getAllUsers();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (User user : users) {
            String text = "%s — %s".formatted(
                    user.getUserName(),
                    user.getChatId()
            );

            rows.add(List.of(
                    InlineKeyboardButton.builder()
                            .text(text)
                            .callbackData("SELECT_USER_" + user.getChatId())
                            .build()
            ));
        }

        bot.sendNewMessage(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("\uD83D\uDC65 Пожалуйста, выберите пользователя, которого вы хотите забанить или разбанить.")
                        .replyMarkup(InlineKeyboardMarkup.builder().keyboard(rows).build())
                        .build()
        );
    }

    private void handleSelectUser(String data, Long chatId) {
        Long userChatId = Long.parseLong(data.substring("SELECT_USER_".length()));

        bot.sendNewMessage(SendMessage.builder()
                .chatId(chatId)
                .text("⚖\uFE0F Выберите действие: забанить или разбанить пользователя?")
                .replyMarkup(AdminKeyboardUtils.buildBanUserActionKeyboard(userChatId))
                .build());
    }

    private void handleBanUser(String data, Long chatId) {
        Long userChatId = Long.parseLong(data.substring("BAN_USER_".length()));

        SendMessage adminMessage = SendMessage.builder()
                .chatId(chatId)
                .text("\uD83D\uDD12 Пользователь успешно заблокирован!")
                .build();

        userService.banUser(userChatId);
        bot.sendNewMessage(adminMessage);
    }

    private void handleUnbanUser(String data, Long chatId) {
        Long userChatId = Long.parseLong(data.substring("UNBAN_USER_".length()));

        SendMessage adminMessage = SendMessage.builder()
                .chatId(chatId)
                .text("\uD83D\uDD13 Пользователь успешно разблокирован!")
                .build();

        userService.unBanUser(userChatId);
        bot.sendNewMessage(adminMessage);
    }
}
