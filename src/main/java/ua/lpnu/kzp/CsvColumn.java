package ua.lpnu.kzp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Позначає поле класу, яке експортується як стовпець CSV-файлу.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CsvColumn {

    /**
     * Повертає назву заголовка CSV-стовпця.
     *
     * @return текст заголовка стовпця
     */
    String value();
}