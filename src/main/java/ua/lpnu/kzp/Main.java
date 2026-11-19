package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Головний клас програми для лабораторної роботи № 5 (Варіант 5: Меню кафе).
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
        Path inputPath = Path.of("data", "input.csv");
        Path outputPath = Path.of("out", "report.txt");
        Path csvExportPath = Path.of("target", "menu-export.csv");

        MenuController controller = new MenuController();

        try {
            controller.process(inputPath, outputPath, csvExportPath);
        } catch (IOException e) {
            System.err.println("Критична помилка роботи з файлами: " + e.getMessage());
            System.exit(1);
        }
    }
}