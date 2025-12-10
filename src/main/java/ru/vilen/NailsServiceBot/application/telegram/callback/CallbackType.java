package ru.vilen.NailsServiceBot.application.telegram.callback;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum CallbackType {

    // User
    BOOK("💅 Новая запись"),
    MY("📖 Мои записи"),
    HELP("🆘 Помощь"),
    DATE("\uD83D\uDCC5 Выбрать дату"),
    SETTINGS("⚙️ Настройки"),
    TIME("⏰ Выбрать время"),
    CONFIRM("✅ Подтвердить запись"),
    CANCEL("❌ Отменить запись "),
    BACK("\uD83D\uDD19 Назад"),
    HOME("\uD83C\uDFE0 Главное меню "),
    REGISTER("Зарегистрироваться"),
    CHANGE_NAME("✏\uFE0F Изменить имя"),
    CHANGE_PHONE("\uD83D\uDCF1 Изменить номер"),

    // Admin
    ADMIN_HOME("\uD83C\uDFE0 Главное меню"),
    APPROVE("✅ Принять запись"),
    REJECT("❌ Отклонить запись"),
    SCHEDULE("\uD83D\uDCC5 Расписание"),
    USERS("\uD83D\uDC65 Пользователи"),
    SCHEDULE_FOR_TODAY("\uD83D\uDCC6 Сегодня"),
    DELETE_USER("\uD83D\uDDD1\uFE0F Удалить"),
    BAN("⛔ Бан"),
    CHANGE_BOOKING("✏\uFE0F Изменить"),
    CHANGE_DATE("\uD83D\uDCC5 Дата"),
    CHANGE_TIME("⏰ Время"),
    CHANGE_TYPE("✨ Тип услуги"),
    ADMIN_SETTINGS("⚙\uFE0F Настройки"),
    BAN_USER("⛔ Бан"),
    UNBAN_USER("✔️ Разбан"),
    DELETE_BOOKING("\uD83D\uDDD1\uFE0F Удалить");

    String buttonText;
}
