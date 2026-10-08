package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 * Клас для тестування основних компонентів програми.
 */
class MainTest {

    @Test
    void testAverageCalculation() {
        // Перевіряємо логіку розрахунку середньої ціни або базових числових операцій
        double totalPrice = 45.50 + 65.00 + 95.00;
        int validCount = 3;
        double average = totalPrice / validCount;

        assertEquals(68.50, average, 0.001, "Середня ціна розрахована неправильно");
    }
}