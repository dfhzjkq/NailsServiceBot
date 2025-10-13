package ru.vilen.NailsServiceBot.application.telegram.callback.admin;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum AdminCallbackType {

    HOME("Главное меню"),
    APPROVE("✅ Принять запись"),
    REJECT("❌ Отклонить запись"),
    VIEW_SCHEDULE("📅 Расписание");

    String buttonText;
}

