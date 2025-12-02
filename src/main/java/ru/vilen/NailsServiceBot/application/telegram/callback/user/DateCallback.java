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
import ru.vilen.NailsServiceBot.entity.BookingType;
import ru.vilen.NailsServiceBot.entity.User;
import ru.vilen.NailsServiceBot.entity.UserStatus;
import ru.vilen.NailsServiceBot.repository.UserRepository;
import ru.vilen.NailsServiceBot.service.BookingService;
import ru.vilen.NailsServiceBot.service.UserService;
import ru.vilen.NailsServiceBot.utils.UserKeyboardUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        log.info("[{}] Callback {} от пользователя {} [id{}]", update.getUpdateId(), getType(), userName, userId);

        if (data.startsWith("TYPE_")) {
            String typeName = data.substring("TYPE_".length());
            BookingType bookingType = BookingType.valueOf(typeName);
            bookingService.saveBookingType(chatId, bookingType);

            bot.sendNewMessage(SendMessage.builder()
                    .chatId(chatId)
                    .text("Отлично! Теперь выбери дату записи 📅")
                    .replyMarkup(UserKeyboardUtils.buildDateInlineKeyboard(YearMonth.now()))
                    .build());
            return;
        }

        if (data.equals("DATE")) {
            sendCalendar(chatId, YearMonth.now());
            return;
        }

        if (data.startsWith("CAL_PREV_")) {
            YearMonth ym = YearMonth.parse(data.substring("CAL_PREV_".length()));
            sendCalendar(chatId, ym.minusMonths(1));
            return;
        }

        if (data.startsWith("CAL_NEXT_")) {
            YearMonth ym = YearMonth.parse(data.substring("CAL_NEXT_".length()));
            sendCalendar(chatId, ym.plusMonths(1));
            return;
        }

        if (data.startsWith("DATE_")) {
            LocalDate date = LocalDate.parse(data.substring("DATE_".length()));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String formattedDate = date.format(formatter);

            bookingService.saveBookingDate(chatId, date);

            User user = userService.getOrCreateUser(chatId);
            user.setUserState(UserStatus.WAITING_BOOK);
            userRepository.save(user);

            List<String> intervals = bookingService.getAvailableIntervals(chatId, date);
            String availableText = intervals.isEmpty()
                    ? "К сожалению, на этот день свободных окон нет 😔"
                    : intervals.stream().map(i -> "🕒 " + i).collect(Collectors.joining("\n"));

            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(String.format("""
                        📅 Ты выбрал дату: %s
                        
                        Вот доступные промежутки, когда можно начать запись:
                        %s
                        
                        ✍️ Отправь мне время сообщением — просто напиши, во сколько хочешь прийти.
                        Например: 09:30, 15:00, 09:45.
                            
                        Я проверю время и сразу создам запись.
                        """, formattedDate, availableText))
                    .build();

            bot.sendNewMessage(message);
        }
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
    public CallbackType getType() {
        return CallbackType.DATE;
    }
}