package ru.vilen.NailsServiceBot.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.vilen.NailsServiceBot.application.telegram.callback.CallbackType;
import ru.vilen.NailsServiceBot.entity.BookingType;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public class AdminKeyboardUtils {
    public static InlineKeyboardMarkup buildMenuInlineKeyboard() {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text(CallbackType.SCHEDULE_FOR_TODAY.getButtonText())
                .callbackData(CallbackType.SCHEDULE_FOR_TODAY.toString())
                .build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text(CallbackType.SCHEDULE.getButtonText())
                .callbackData(CallbackType.SCHEDULE.toString())
                .build());


//        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text(CallbackType.USERS.getButtonText())
                .callbackData(CallbackType.USERS.toString())
                .build());

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2))
                .build();
    }

    public static InlineKeyboardMarkup buildScheduleInlineKeyboard() {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text(CallbackType.CHANGE_BOOKING.getButtonText())
                .callbackData(CallbackType.CHANGE_BOOKING.toString())
                .build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text(CallbackType.DELETE_BOOKING.getButtonText())
                .callbackData(CallbackType.DELETE_BOOKING.toString())
                .build());

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(InlineKeyboardButton.builder()
                .text(CallbackType.ADMIN_HOME.getButtonText())
                .callbackData(CallbackType.ADMIN_HOME.toString())
                .build());

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3))
                .build();
    }

    public static InlineKeyboardMarkup buildScheduleForTodayInlineKeyboard() {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text(CallbackType.ADMIN_HOME.getButtonText())
                .callbackData(CallbackType.ADMIN_HOME.toString())
                .build());

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1))
                .build();
    }

    public static InlineKeyboardMarkup buildDecisionInlineKeyboard(Long bookingId) {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text(CallbackType.APPROVE.getButtonText())
                .callbackData(CallbackType.APPROVE + "_" + bookingId)
                .build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text(CallbackType.REJECT.getButtonText())
                .callbackData(CallbackType.REJECT + "_" + bookingId)
                .build());

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2))
                .build();
    }

    public static InlineKeyboardMarkup buildUsersInlineKeyboard() {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text(CallbackType.DELETE_USER.getButtonText())
                .callbackData(CallbackType.DELETE_USER.toString())
                .build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text(CallbackType.BAN_USER.getButtonText())
                .callbackData(CallbackType.BAN_USER.toString())
                .build());

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(InlineKeyboardButton.builder()
                .text(CallbackType.ADMIN_HOME.getButtonText())
                .callbackData(CallbackType.ADMIN_HOME.toString())
                .build());

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3))
                .build();
    }

    public static InlineKeyboardMarkup buildChangeBookingActionKeyboard(Long bookingId) {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text(CallbackType.CHANGE_DATE.getButtonText())
                .callbackData("CHANGE_DATE_"  + bookingId)
                .build());
        row1.add(InlineKeyboardButton.builder()
                .text(CallbackType.CHANGE_TIME.getButtonText())
                .callbackData("CHANGE_TIME_"  + bookingId)
                .build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text(CallbackType.CHANGE_TYPE.getButtonText())
                .callbackData("CHANGE_TYPE_"  + bookingId)
                .build());

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2))
                .build();
    }

    public static InlineKeyboardMarkup buildChangeTypeInlineKeyboard(Long bookingId) {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text(BookingType.MANICURE.getLabel())
                .callbackData("ADMIN_TYPE_MANICURE_" +  bookingId)
                .build());

        row1.add(InlineKeyboardButton.builder()
                .text(BookingType.PEDICURE.getLabel())
                .callbackData("ADMIN_TYPE_PEDICURE_"  +  bookingId)
                .build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text(BookingType.BOTH.getLabel())
                .callbackData("ADMIN_TYPE_BOTH_"  +  bookingId)
                .build());

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2))
                .build();
    }

    public static InlineKeyboardMarkup buildCalendar(Long bookingId, LocalDate shownMonth) {

        YearMonth ym = YearMonth.from(shownMonth);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        String[] monthNames = {
                "Январь", "Февраль", "Март", "Апрель",
                "Май", "Июнь", "Июль", "Август",
                "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        };

        String monthTitle = monthNames[ym.getMonthValue() - 1] + " " + ym.getYear();

        // Заголовок месяца
        keyboard.add(List.of(
                InlineKeyboardButton.builder().text("◀️").callbackData("ADMIN_CAL_PREV_" + bookingId + "_" + ym).build(),
                InlineKeyboardButton.builder().text(monthTitle).callbackData("IGNORE").build(),
                InlineKeyboardButton.builder().text("▶️").callbackData("ADMIN_CAL_NEXT_" + bookingId + "_" + ym).build()
        ));

        // Дни недели
        keyboard.add(Arrays.asList(
                btn("Пн"), btn("Вт"), btn("Ср"), btn("Чт"),
                btn("Пт"), btn("Сб"), btn("Вс")
        ));

        // Построение дней
        LocalDate firstDay = ym.atDay(1);
        int len = ym.lengthOfMonth();
        int dow = firstDay.getDayOfWeek().getValue(); // 1–7, Пн=1

        List<InlineKeyboardButton> row = new ArrayList<>();

        // Пустые слоты перед первым днём
        for (int i = 1; i < dow; i++) {
            row.add(btn(" "));
        }

        LocalDate today = LocalDate.now();

        for (int day = 1; day <= len; day++) {
            LocalDate current = ym.atDay(day);

            InlineKeyboardButton b;

            if (current.isBefore(today)) {
                // прошлая дата — неактивная
                b = InlineKeyboardButton.builder()
                        .text("·" + day + "·")
                        .callbackData("IGNORE")
                        .build();
            } else {
                // активная дата
                b = InlineKeyboardButton.builder()
                        .text(String.valueOf(day))
                        .callbackData("SET_NEW_DATE_" + bookingId + "_" + current)
                        .build();
            }

            row.add(b);

            if (row.size() == 7) {
                keyboard.add(row);
                row = new ArrayList<>();
            }
        }

        // Хвостовые пустоты
        if (!row.isEmpty()) {
            while (row.size() < 7) row.add(btn(" "));
            keyboard.add(row);
        }

        return InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();
    }

    private static InlineKeyboardButton btn(String text) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData("IGNORE")
                .build();
    }
}
