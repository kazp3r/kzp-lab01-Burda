package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * Описує незмінний запис про позицію в меню кафе.
 */
public final class MenuItem {
    private final String supplier;
    private final String name;
    private final String category;
    private final double price;
    private final int weightG;
    private final int prepMin;

    /**
     * Створює коректний запис про позицію в меню після перевірки полів.
     *
     * @param supplier постачальник
     * @param name     назва страви/напою
     * @param category категорія
     * @param price    ціна
     * @param weightG  вага в грамах
     * @param prepMin  час приготування у хвилинах
     */
    public MenuItem(String supplier, String name, String category, double price, int weightG, int prepMin) {
        Objects.requireNonNull(supplier, "Постачальник не може бути null");
        Objects.requireNonNull(name, "Назва не може бути null");
        Objects.requireNonNull(category, "Категорія не може бути null");

        if (supplier.isBlank() || name.isBlank() || category.isBlank()) {
            throw new IllegalArgumentException("Текстові поля (постачальник, назва, категорія) не можуть бути порожніми");
        }
        if (price < 0 || !Double.isFinite(price)) {
            throw new IllegalArgumentException("Ціна має бути скінченною і не від'ємною");
        }
        if (weightG < 0 || prepMin < 0) {
            throw new IllegalArgumentException("Вага та час приготування не можуть бути від'ємними");
        }

        this.supplier = supplier;
        this.name = name;
        this.category = category;
        this.price = price;
        this.weightG = weightG;
        this.prepMin = prepMin;
    }

    /**
     * Створює запис із CSV-рядка.
     *
     * @param line рядок у форматі: постачальник;назва;категорія;ціна;вага;час
     * @return створений об'єкт MenuItem
     */
    public static MenuItem fromCsv(String line) {
        Objects.requireNonNull(line, "Рядок не може бути null");
        String[] fields = line.split(";", -1);
        
        if (fields.length != 6) {
            throw new IllegalArgumentException("Очікується 6 полів");
        }
        
        try {
            return new MenuItem(
                    fields[0].trim(),
                    fields[1].trim(),
                    fields[2].trim(),
                    Double.parseDouble(fields[3].trim()),
                    Integer.parseInt(fields[4].trim()),
                    Integer.parseInt(fields[5].trim())
            );
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Числове поле має помилковий формат", exception);
        }
    }

    public String getSupplier() {
        return supplier;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getWeightG() {
        return weightG;
    }

    public int getPrepMin() {
        return prepMin;
    }

    /**
     * Повертає текстове подання запису для звіту.
     */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s: %s (%s) - %.2f грн, %d г, %d хв",
                supplier, name, category, price, weightG, prepMin);
    }
}