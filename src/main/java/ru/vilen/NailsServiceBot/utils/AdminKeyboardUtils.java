package ru.vilen.NailsServiceBot.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.vilen.NailsServiceBot.application.telegram.callback.CallbackType;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class AdminKeyboardUtils {
    public static InlineKeyboardMarkup buildMenuInlineKeyboard() {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text(CallbackType.SCHEDULE.getButtonText())
                .callbackData(CallbackType.SCHEDULE.toString())
                .build());

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1))
                .build();
    }

    public static InlineKeyboardMarkup buildScheduleInlineKeyboard() {
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
}
