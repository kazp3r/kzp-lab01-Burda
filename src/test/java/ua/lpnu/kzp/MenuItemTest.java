package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class MenuItemTest {

    /* Перевіряє різну поведінку підтипів (Поліморфізм) */
    @Test
    void subtypesCalculateCostDifferently() {
        MenuItem dish = new Dish("Постачальник", "Борщ", 100.0, 300, 20);
        MenuItem drink = new Drink("Постачальник", "Сік", 100.0, 200, 5);
        MenuItem dessert = new Dessert("Постачальник", "Торт", 100.0, 150, 10);

        // При однаковій базовій ціні 100, поліморфний метод має повернути різні значення
        assertEquals(115.0, dish.calculateCost(), 0.0001); // 100 * 1.15
        assertEquals(105.0, drink.calculateCost(), 0.0001); // 100 + 5.0
        assertEquals(110.0, dessert.calculateCost(), 0.0001); // 100 * 1.10
    }

    /* Перевіряє фабричний метод та Enum */
    @Test
    void fromCsvCreatesCorrectSubtype() {
        MenuItem item = MenuItem.fromCsv("Постачальник;Кава;Напій;60.0;200;5");
        assertEquals(Drink.class, item.getClass());
        assertEquals(MenuKind.DRINK, item.getKind());
    }

    /* Перевіряє, що відхиляється невідома категорія */
    @Test
    void fromCsvRejectsUnknownCategory() {
        assertThrows(IllegalArgumentException.class,
                () -> MenuItem.fromCsv("Постачальник;Капучино;Щось;65.50;200;5"));
    }

    /* Перевіряє відсутність дублювання рівних об'єктів у HashSet */
    @Test
    void equalObjectsHaveOneSetEntry() {
        Set<MenuItem> items = new HashSet<>();
        items.add(new Dish("Постачальник", "Борщ", 100.0, 300, 20));
        items.add(new Dish("Постачальник", "Борщ", 100.0, 300, 20));
        
        assertEquals(1, items.size());
    }

    /* Перевіряє, що різні класи не рівні навіть з однаковими назвами */
    @Test
    void differentSubtypesAreNotEqual() {
        MenuItem dish = new Dish("Шеф", "Фірмове", 100.0, 300, 20);
        MenuItem drink = new Drink("Шеф", "Фірмове", 100.0, 200, 5);
        
        assertNotEquals(dish, drink);
    }
}