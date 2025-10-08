package ru.vilen.NailsServiceBot.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.vilen.NailsServiceBot.application.telegram.callback.CallbackType;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@UtilityClass
public class KeyboardUtils {

    /*public static ReplyKeyboardMarkup buildMainReplyKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(KeyboardButton.builder().text("\t\uD83D\uDCDE Отправить телефон ").build());

        return ReplyKeyboardMarkup.builder()
                .keyboardRow(row1)
                .resizeKeyboard(true)
                .selective(false)
                .isPersistent(true)
                .build();
    }*/

    public static InlineKeyboardMarkup buildMenuInlineKeyboard() {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text(CallbackType.BOOK.getButtonText())
                .callbackData(CallbackType.BOOK.toString())
                .build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text(CallbackType.MY.getButtonText())
                .callbackData(CallbackType.MY.toString())
                .build());

        row2.add(InlineKeyboardButton.builder()
                .text(CallbackType.SETTINGS.getButtonText())
                .callbackData(CallbackType.SETTINGS.toString())
                .build());

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2))
                .build();
    }

    public static InlineKeyboardMarkup buildBookInlineKeyboard() {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text(CallbackType.DATE.getButtonText())
                .callbackData(CallbackType.DATE.toString())
                .build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text(CallbackType.HOME.getButtonText())
                .callbackData(CallbackType.HOME.toString())
                .build());

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2))
                .build();
    }

    public static InlineKeyboardMarkup buildHomeInlineKeyboard() {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text(CallbackType.HOME.getButtonText())
                .callbackData(CallbackType.HOME.toString())
                .build());

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1))
                .build();
    }

    public static InlineKeyboardMarkup buildRegisterInlineKeyboard() {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text(CallbackType.REGISTER.getButtonText())
                .callbackData(CallbackType.REGISTER.toString())
                .build());

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1))
                .build();
    }

    public static InlineKeyboardMarkup buildSettingsInlineKeyboard() {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text(CallbackType.CHANGE_NAME.getButtonText())
                .callbackData(CallbackType.CHANGE_NAME.toString())
                .build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text(CallbackType.CHANGE_PHONE.getButtonText())
                .callbackData(CallbackType.CHANGE_PHONE.toString())
                .build());

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(InlineKeyboardButton.builder()
                .text(CallbackType.HOME.getButtonText())
                .callbackData(CallbackType.HOME.toString())
                .build());

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3))
                .build();
    }

    public static InlineKeyboardMarkup buildDateInlineKeyboard(YearMonth yearMonth) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        String[] monthNames = {
                "Январь", "Февраль", "Март", "Апрель",
                "Май", "Июнь", "Июль", "Август",
                "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        };

        String monthTitle = monthNames[yearMonth.getMonthValue() - 1] + " " + yearMonth.getYear();

        // Заголовок месяца
        List<InlineKeyboardButton> headerRow = new ArrayList<>();
        headerRow.add(InlineKeyboardButton.builder().text("◀️").callbackData("CAL_PREV_" + yearMonth).build());
        headerRow.add(InlineKeyboardButton.builder().text(monthTitle).callbackData("IGNORE").build());
        headerRow.add(InlineKeyboardButton.builder().text("▶️").callbackData("CAL_NEXT_" + yearMonth).build());
        keyboard.add(headerRow);

        // Дни недели
        List<InlineKeyboardButton> weekDays = Arrays.asList(
                InlineKeyboardButton.builder().text("Пн").callbackData("IGNORE").build(),
                InlineKeyboardButton.builder().text("Вт").callbackData("IGNORE").build(),
                InlineKeyboardButton.builder().text("Ср").callbackData("IGNORE").build(),
                InlineKeyboardButton.builder().text("Чт").callbackData("IGNORE").build(),
                InlineKeyboardButton.builder().text("Пт").callbackData("IGNORE").build(),
                InlineKeyboardButton.builder().text("Сб").callbackData("IGNORE").build(),
                InlineKeyboardButton.builder().text("Вс").callbackData("IGNORE").build()
        );
        keyboard.add(weekDays);

        // Построение дней месяца
        LocalDate firstDay = yearMonth.atDay(1);
        int lengthOfMonth = yearMonth.lengthOfMonth();
        int dayOfWeek = firstDay.getDayOfWeek().getValue();

        List<InlineKeyboardButton> weekRow = new ArrayList<>();

        // Пустые слоты перед первым днём
        for (int i = 1; i < dayOfWeek; i++) {
            weekRow.add(InlineKeyboardButton.builder().text(" ").callbackData("IGNORE").build());
        }

        for (int day = 1; day <= lengthOfMonth; day++) {
            weekRow.add(InlineKeyboardButton.builder()
                    .text(String.valueOf(day))
                    .callbackData("DATE_" + yearMonth + "-" + day)
                    .build());

            if (weekRow.size() == 7) {
                keyboard.add(weekRow);
                weekRow = new ArrayList<>();
            }
        }

        // Заполняем последнюю неделю пустыми
        if (!weekRow.isEmpty()) {
            while (weekRow.size() < 7) {
                weekRow.add(InlineKeyboardButton.builder().text(" ").callbackData("IGNORE").build());
            }
            keyboard.add(weekRow);
        }

        // Кнопка "Домой"
        List<InlineKeyboardButton> homeRow = new ArrayList<>();
        homeRow.add(InlineKeyboardButton.builder()
                .text(CallbackType.HOME.getButtonText())
                .callbackData(CallbackType.HOME.toString())
                .build());
        keyboard.add(homeRow);

        return InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();
    }

    public static InlineKeyboardMarkup buildMyInlineKeyboard() {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text(CallbackType.CANCEL.getButtonText())
                .callbackData(CallbackType.CANCEL.toString())
                .build());

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1))
                .build();
    }

    public static InlineKeyboardMarkup buildCancelInlineKeyboard() {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text(CallbackType.MY.getButtonText())
                .callbackData(CallbackType.MY.toString())
                .build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text(CallbackType.HOME.getButtonText())
                .callbackData(CallbackType.HOME.toString())
                .build());

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2))
                .build();
    }
}
