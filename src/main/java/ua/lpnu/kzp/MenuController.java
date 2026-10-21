package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Контролер для керування процесом обробки меню кафе.
 */
public class MenuController {

    /**
     * Читає файл, обробляє записи та генерує звіт.
     *
     * @param inputPath  шлях до вхідного файлу
     * @param outputPath шлях до файлу звіту
     * @throws IOException якщо виникає помилка читання/запису
     */
    public void process(Path inputPath, Path outputPath) throws IOException {
        List<String> lines = Files.readAllLines(inputPath, StandardCharsets.UTF_8);
        List<MenuItem> items = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        double totalPrice = 0.0;
        int maxWeight = 0;
        int maxPrepTime = 0;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            try {
                // Використовуємо наш фабричний метод для створення об'єкта
                MenuItem item = MenuItem.fromCsv(line);
                items.add(item);
                
                totalPrice += item.getPrice();
                maxWeight = Math.max(maxWeight, item.getWeightG());
                maxPrepTime = Math.max(maxPrepTime, item.getPrepMin());
            } catch (IllegalArgumentException e) {
                // Зберігаємо помилку разом із номером рядка (нумерація з 1)
                errors.add(String.format("Рядок %d: %s", i + 1, e.getMessage()));
            }
        }

        // Обчислюємо середню ціну та створюємо record
        double avgPrice = items.isEmpty() ? 0.0 : totalPrice / items.size();
        MenuSummary summary = new MenuSummary(items.size(), avgPrice, maxWeight, maxPrepTime);

        generateReport(summary, errors, outputPath);
    }

    private void generateReport(MenuSummary summary, List<String> errors, Path outputPath) throws IOException {
        StringBuilder report = new StringBuilder();
        
        report.append(String.format(Locale.ROOT, "Коректних записів: %d%n", summary.count()));
        report.append(String.format(Locale.ROOT, "Середня ціна: %.2f грн%n", summary.averagePrice()));
        report.append(String.format(Locale.ROOT, "Найбільша вага: %d г%n", summary.highestWeight()));
        report.append(String.format(Locale.ROOT, "Найдовший час приготування: %d хв%n", summary.longestPrepTime()));
        report.append(String.format(Locale.ROOT, "Помилок: %d%n", errors.size()));

        for (String error : errors) {
            report.append(error).append(System.lineSeparator());
        }

        String reportText = report.toString();
        
        // Виводимо в консоль
        System.out.print(reportText);

        // Зберігаємо у файл, обходячи попередження SpotBugs про NullPointer
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(outputPath, reportText, StandardCharsets.UTF_8);
    }
}