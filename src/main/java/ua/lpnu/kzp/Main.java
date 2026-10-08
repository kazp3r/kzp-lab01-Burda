package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Головний клас програми для лабораторної роботи № 1 (Варіант 5: Меню кафе).
 */
public final class Main {

    private Main() {
        // Забороняє створення екземплярів службового класу.
    }

    /**
     * Точка входу до програми.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        if (args.length > 0 && "--help".equals(args[0])) {
            System.out.printf("Використання: java -jar lab01-1.0.0.jar [--help] [--input <файл>] [--output <файл>]%n");
            return;
        }

        Path inputPath = Path.of("data", "input.csv");
        Path outputPath = Path.of("out", "report.txt");

        for (int i = 0; i < args.length; i++) {
            if ("--input".equals(args[i]) && i + 1 < args.length) {
                inputPath = Path.of(args[i + 1]);
                i++;
            } else if ("--output".equals(args[i]) && i + 1 < args.length) {
                outputPath = Path.of(args[i + 1]);
                i++;
            }
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(inputPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.printf("Помилка читання файлу: %s%n", e.getMessage());
            return;
        }

        List<String> errors = new ArrayList<>();
        int validCount = 0;
        double totalPrice = 0.0;
        int maxWeight = Integer.MIN_VALUE;
        int maxPrepMin = Integer.MIN_VALUE;

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] fields = line.split(";", -1);
            if (fields.length != 6) {
                errors.add("Рядок %d: очікується 6 полів, знайдено %d".formatted(index + 1, fields.length));
                continue;
            }

            if (fields[0].isBlank() || fields[1].isBlank() || fields[2].isBlank()) {
                errors.add("Рядок %d: порожнє обов'язкове текстове поле".formatted(index + 1));
                continue;
            }

            try {
                double price = Double.parseDouble(fields[3]);
                int weightG = Integer.parseInt(fields[4]);
                int prepMin = Integer.parseInt(fields[5]);

                if (price < 0 || weightG < 0 || prepMin < 0) {
                    errors.add("Рядок %d: від'ємне числове значення".formatted(index + 1));
                    continue;
                }

                validCount++;
                totalPrice += price;
                maxWeight = Math.max(maxWeight, weightG);
                maxPrepMin = Math.max(maxPrepMin, prepMin);

            } catch (NumberFormatException exception) {
                errors.add("Рядок %d: числове поле має помилковий формат".formatted(index + 1));
            }
        }

        double averagePrice = validCount == 0 ? 0.0 : totalPrice / validCount;

        StringBuilder report = new StringBuilder();
        report.append(String.format(Locale.ROOT, "Коректних записів: %d%n", validCount));
        report.append(String.format(Locale.ROOT, "Середня ціна: %.2f грн%n", averagePrice));
        report.append(String.format(Locale.ROOT, "Найбільша вага: %d г%n", validCount == 0 ? 0 : maxWeight));
        report.append(String.format(Locale.ROOT, "Найдовший час приготування: %d хв%n", validCount == 0 ? 0 : maxPrepMin));
        report.append(String.format(Locale.ROOT, "Помилок: %d%n", errors.size()));
        for (String err : errors) {
            report.append(err).append(System.lineSeparator());
        }

        System.out.print(report.toString());

        try {
            Path parent = outputPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(outputPath, report.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.printf("Помилка запису файлу звіту: %s%n", e.getMessage());
        }
    }
}