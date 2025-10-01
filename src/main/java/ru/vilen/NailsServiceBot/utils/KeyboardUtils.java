package ru.vilen.NailsServiceBot.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.vilen.NailsServiceBot.application.telegram.callback.CallbackType;

import java.util.ArrayList;
import java.util.List;

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

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(InlineKeyboardButton.builder()
                .text(CallbackType.SETTINGS.getButtonText())
                .callbackData(CallbackType.SETTINGS.toString())
                .build());

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3))
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

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2))
                .build();
    }
}
