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
import ru.vilen.NailsServiceBot.entity.User;
import ru.vilen.NailsServiceBot.entity.UserStatus;
import ru.vilen.NailsServiceBot.service.BookingService;
import ru.vilen.NailsServiceBot.service.UserService;
import ru.vilen.NailsServiceBot.utils.UserKeyboardUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HomeCallback implements Callback {

    TelegramBot bot;
    UserService userService;
    BookingService bookingService;

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
        bookingService.clearUnfinishedBooking(chatId);
        User user = userService.getOrCreateUser(chatId);
        user.setUserState(UserStatus.REGISTERED);
        String name = user.getUserName();

        SendMessage homeMessage = SendMessage.builder()
                .chatId(chatId)
                .text(String.format("""
                        ✨ Привет снова, %s! ✨
                        Рад тебя видеть! Что хочешь сделать сегодня?
                        
                        💅 Записаться на маникюр – выбрать дату и время.
                        📖 Мои записи – посмотреть или отменить существующие записи.
                        ⚙️ Настройки – изменить свои данные.
                        
                        """, name))
                .build();

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Выбери действие ниже ⬇\uFE0F")
                .replyMarkup(UserKeyboardUtils.buildMenuInlineKeyboard())
                .build();

        bot.sendNewMessage(homeMessage);
        bot.sendNewMessage(message);
    }

    @Override
    public UserCallbackType getType() {
        return UserCallbackType.HOME;
    }
}
