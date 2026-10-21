package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Головний клас програми для лабораторної роботи № 2 (Варіант 5: Меню кафе).
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
        System.setOut(new java.io.PrintStream(new java.io.FileOutputStream(java.io.FileDescriptor.out), true, java.nio.charset.StandardCharsets.UTF_8));
        // Визначаємо шляхи до файлів
        Path inputPath = Path.of("data", "input.csv");
        Path outputPath = Path.of("out", "report.txt");

        // Створюємо контролер і запускаємо обробку
        MenuController controller = new MenuController();

        try {
            controller.process(inputPath, outputPath);
        } catch (IOException e) {
            System.err.println("Критична помилка роботи з файлами: " + e.getMessage());
            System.exit(1); // Завершуємо програму з кодом помилки
        }
    }
}