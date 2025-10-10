package ru.vilen.NailsServiceBot.application.telegram.callback;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum CallbackType {

    BOOK("💅 Записаться на маникюр"),
    MY("📖 Мои записи"),
    HELP("❓ Помощь"),
    DATE("\uD83D\uDCC5 Выбрать дату "),
    SETTINGS("⚙️ Настройки"),
    TIME("⏰ Выбрать время"),
    CONFIRM("✅ Подтвердить запись"),
    CANCEL("❌ Отменить запись "),
    BACK("\uD83D\uDD19 Назад"),
    HOME("\uD83C\uDFE0 Главное меню "),
    REGISTER("Зарегистрироваться"),
    CHANGE_NAME("✏\uFE0F Изменить имя"),
    CHANGE_PHONE("\uD83D\uDCF1 Изменить номер");

    String buttonText;
}
