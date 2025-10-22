package ru.vilen.NailsServiceBot.application.telegram.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.vilen.NailsServiceBot.application.telegram.TelegramBot;
import ru.vilen.NailsServiceBot.entity.User;
import ru.vilen.NailsServiceBot.entity.UserStatus;
import ru.vilen.NailsServiceBot.service.BookingService;
import ru.vilen.NailsServiceBot.service.UserService;
import ru.vilen.NailsServiceBot.utils.UserKeyboardUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StartCommand implements Command {

    TelegramBot bot;
    UserService userService;
    private final BookingService bookingService;

    @Override
    public void apply(Update update) {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        String userName = update.getMessage().getFrom().getUserName();

        log.info (
                "[{}] Команда {} от пользователя {} [id={}]",
                update.getUpdateId(),
                getType(),
                userName,
                userId
        );

        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(chatId)
                .messageId(update.getMessage().getMessageId())
                .build();
        bot.deleteMessage(deleteMessage);

        bookingService.clearUnfinishedBooking(chatId);
        User user = userService.getOrCreateUser(chatId);

        if (user.getUserState() == null) {
            SendMessage startMessage = SendMessage.builder()
                    .chatId(chatId)
                    .text("""
                        ✨ Привет, я бот для записи к мастеру по маникюру! ✨
                        Я помогу тебе быстро и удобно записаться на свободное время.
                        
                        📋 Что нужно сделать:
                        	1.	Напиши своё имя 📝
                        	2.	Укажи номер телефона 📱
                        	3.	Выбери удобное время для записи ⏰
                        
                        После этого я сохраню заявку и мастер свяжется с тобой для подтверждения 💖
                        """)
                    .replyMarkup(UserKeyboardUtils.buildRegisterInlineKeyboard())
                    .build();
            bot.sendNewMessage(startMessage);
        } else if (user.getUserState() == UserStatus.WAITING_NAME) {
            bot.sendNewMessage(
                    SendMessage.builder()
                            .chatId(chatId)
                            .text("Регистрация не завершена 😊\nПожалуйста, отправь своё имя:")
                            .build()
            );
        } else if (user.getUserState() == UserStatus.WAITING_PHONE) {
            bot.sendNewMessage(
                    SendMessage.builder()
                            .chatId(chatId)
                            .text("Мы почти закончили! 📱\nПожалуйста, отправь свой номер телефона:")
                            .build()
            );
        } if (user.getUserState() == UserStatus.REGISTERED || user.getUserState() == UserStatus.WAITING_BOOK) {
            bot.sendNewMessage(
                    SendMessage.builder()
                            .chatId(chatId)
                            .text(String.format("""
                        ✨ Привет снова, %s! ✨
                        Рад тебя видеть! Что хочешь сделать сегодня?
                        
                        💅 Записаться на маникюр – выбрать дату и время.
                        📖 Мои записи – посмотреть или отменить существующие записи.
                        ⚙️ Настройки – изменить свои данные.
                        
                        """, user.getUserName()))
                            .build()
            );

            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text("Выбери действие ниже ⬇\uFE0F")
                    .replyMarkup(UserKeyboardUtils.buildMenuInlineKeyboard())
                    .build();

            bot.sendNewMessage(message);
        } else {
            SendMessage successMessage = SendMessage.builder()
                    .chatId(chatId)
                    .text("Ты уже успешно зарегистрировался!  \n" +
                            "Переходи в главное меню по кнопке ниже ⬇\uFE0F")
                    .replyMarkup(UserKeyboardUtils.buildHomeInlineKeyboard())
                    .build();
            bot.sendNewMessage(successMessage);
        }
    }

    @Override
    public CommandType getType() {
        return CommandType.START;
    }
}
