package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class MenuItemTest {

    /* Перевіряє успішне створення об'єкта з коректного рядка */
    @Test
    void fromCsvCreatesValidEntity() {
        MenuItem item = MenuItem.fromCsv("Постачальник 1;Капучино;Напої;65.50;200;5");
        
        assertEquals("Постачальник 1", item.getSupplier());
        assertEquals("Капучино", item.getName());
        assertEquals("Напої", item.getCategory());
        assertEquals(65.50, item.getPrice(), 0.0001);
        assertEquals(200, item.getWeightG());
        assertEquals(5, item.getPrepMin());
    }

    /* Перевіряє, що конструктор відхиляє порожні текстові поля */
    @Test
    void constructorRejectsEmptyText() {
        assertThrows(IllegalArgumentException.class,
                () -> new MenuItem("", "Капучино", "Напої", 65.50, 200, 5));
    }

    /* Перевіряє, що конструктор відхиляє від'ємну ціну */
    @Test
    void constructorRejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new MenuItem("Постачальник 1", "Капучино", "Напої", -10.0, 200, 5));
    }

    /* Перевіряє, що конструктор відхиляє від'ємну вагу */
    @Test
    void constructorRejectsNegativeWeight() {
        assertThrows(IllegalArgumentException.class,
                () -> new MenuItem("Постачальник 1", "Капучино", "Напої", 65.50, -50, 5));
    }

    /* Перевіряє, що фабричний метод відхиляє рядок з текстом замість числа */
    @Test
    void fromCsvRejectsInvalidNumber() {
        assertThrows(IllegalArgumentException.class,
                () -> MenuItem.fromCsv("Постачальник 1;Капучино;Напої;помилка;200;5"));
    }

    /* Перевіряє, що фабричний метод відхиляє неправильну кількість полів у рядку */
    @Test
    void fromCsvRejectsWrongNumberOfFields() {
        assertThrows(IllegalArgumentException.class,
                () -> MenuItem.fromCsv("Постачальник 1;Капучино;Напої;65.50"));
    }

    /* Перевіряє рівність значень об'єктів record (Value Equality) */
    @Test
    void recordUsesValueEquality() {
        MenuSummary first = new MenuSummary(10, 55.0, 350, 20);
        MenuSummary second = new MenuSummary(10, 55.0, 350, 20);
        
        assertEquals(first, second);
    }
    
    /* Перевіряє валідацію всередині record */
    @Test
    void recordRejectsNegativeValues() {
        assertThrows(IllegalArgumentException.class,
                () -> new MenuSummary(-1, 55.0, 350, 20));
    }
}