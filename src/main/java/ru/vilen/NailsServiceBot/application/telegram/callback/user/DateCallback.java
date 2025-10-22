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
import ru.vilen.NailsServiceBot.entity.Book;
import ru.vilen.NailsServiceBot.entity.BookingStatus;
import ru.vilen.NailsServiceBot.entity.User;
import ru.vilen.NailsServiceBot.entity.UserStatus;
import ru.vilen.NailsServiceBot.repository.UserRepository;
import ru.vilen.NailsServiceBot.service.BookingService;
import ru.vilen.NailsServiceBot.service.UserService;
import ru.vilen.NailsServiceBot.utils.UserKeyboardUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DateCallback implements Callback {

    TelegramBot bot;
    BookingService bookingService;
    UserService userService;
    UserRepository userRepository;

    @Override
    public void apply(Update update) {
        Long userId = update.getCallbackQuery().getFrom().getId();
        String userName = update.getCallbackQuery().getFrom().getUserName();
        String data = update.getCallbackQuery().getData();
        log.info("[{}] Callback {} от пользователя {} [id{}]",
                update.getUpdateId(),
                getType(),
                userName,
                userId);

        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (data.equals("DATE")) {
            sendCalendar(chatId, YearMonth.now());
        } else if (data.startsWith("CAL_PREV_")) {
            YearMonth ym = YearMonth.parse(data.substring("CAL_PREV_".length()));
            sendCalendar(chatId, ym.minusMonths(1));
        } else if (data.startsWith("CAL_NEXT_")) {
            YearMonth ym = YearMonth.parse(data.substring("CAL_NEXT_".length()));
            sendCalendar(chatId, ym.plusMonths(1));
        }

        LocalDate date = LocalDate.parse(data.substring("DATE_".length()));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String formattedDate = date.format(formatter);

        bookingService.saveBookingDate(chatId, date);

        User user = userService.getOrCreateUser(chatId);
        user.setUserState(UserStatus.WAITING_BOOK);
        userRepository.save(user);

        SendMessage askTime = SendMessage.builder()
                .chatId(chatId)
                .text("📅 Отлично, дата выбрана: " + formattedDate +
                        "\n⏰ Теперь напиши время вручную (например: 14:30)")
                .build();
        bot.sendNewMessage(askTime);

    }

    private void sendCalendar(Long chatId, YearMonth ym) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("📅 Пожалуйста, выбери дату ⬇️")
                .replyMarkup(UserKeyboardUtils.buildDateInlineKeyboard(ym))
                .build();
        bot.sendNewMessage(message);
    }

    @Override
    public UserCallbackType getType() {
        return UserCallbackType.DATE;
    }
}
