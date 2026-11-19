package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Locale;

/**
 * Контролер для керування процесом обробки меню кафе, збереження у сховищі та CSV-експорту.
 */
public class MenuController {

    /**
     * Читає вхідний файл, обробляє записи за допомогою Stream API та генерує текстовий звіт.
     *
     * @param inputPath  шлях до вхідного файлу
     * @param outputPath шлях до файлу звіту
     * @throws IOException якщо виникає помилка читання або запису
     */
    public void process(Path inputPath, Path outputPath) throws IOException {
        process(inputPath, outputPath, null);
    }

    /**
     * Читає вхідний файл, виконує потоковий аналіз, зберігає записи в {@link Repository}
     * та експортує їх у CSV-файл за вказаним шляхом.
     *
     * @param inputPath     шлях до вхідного файлу
     * @param outputPath    шлях до файлу текстового звіту
     * @param csvExportPath шлях до вихідного CSV-файлу (або {@code null}, якщо експорт не потрібен)
     * @throws IOException якщо виникає помилка файлової системи або CSV-експорту
     */
    public void process(Path inputPath, Path outputPath, Path csvExportPath) throws IOException {
        List<String> lines = Files.readAllLines(inputPath, StandardCharsets.UTF_8);
        List<MenuItem> items = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            try {
                items.add(MenuItem.fromCsv(line));
            } catch (IllegalArgumentException e) {
                errors.add(String.format(Locale.ROOT, "Рядок %d: %s", i + 1, e.getMessage()));
            }
        }

        DoubleSummaryStatistics priceStats = MenuReport.priceStatistics(items);
        int maxWeight = MenuReport.maxWeight(items);
        int maxPrepTime = MenuReport.maxPrepTime(items);

        MenuSummary summary = new MenuSummary(
                items.size(),
                priceStats.getAverage(),
                maxWeight,
                maxPrepTime
        );

        generateReport(summary, errors, outputPath);

        if (csvExportPath != null) {
            Repository<MenuItemRecord> repository = new Repository<>();
            for (MenuItem item : items) {
                repository.add(MenuItemRecord.fromMenuItem(item));
            }
            try {
                CsvExporter.write(csvExportPath, MenuItemRecord.class, repository.all());
            } catch (DataStorageException exception) {
                throw new IOException("Помилка CSV-експорту: " + exception.getMessage(), exception);
            }
        }
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

        System.out.print(reportText);

        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(outputPath, reportText, StandardCharsets.UTF_8);
    }
}