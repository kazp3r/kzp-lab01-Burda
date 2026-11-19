package ua.lpnu.kzp;

import java.util.List;
import java.util.Objects;

/**
 * Незмінний запис позиції меню кафе (Варіант 5) для CSV-експорту та відновлення.
 * Порядок CSV-стовпців за іменами полів: {@code категорія,назва,час готування,ціна,вага}.
 */
public final class MenuItemRecord {

    @CsvColumn("назва")
    private final String name;

    @CsvColumn("категорія")
    private final MenuKind category;

    @CsvColumn("ціна")
    private final double price;

    @CsvColumn("вага")
    private final int weightGrams;

    @CsvColumn("час готування")
    private final int preparationMinutes;

    /**
     * Створює перевірений запис позиції меню кафе.
     *
     * @param name               назва страви (ключ round-trip)
     * @param category           категорія страви
     * @param price              ціна позиції (грн)
     * @param weightGrams        вага позиції (г)
     * @param preparationMinutes час приготування (хв)
     * @throws IllegalArgumentException якщо порушено інваріанти полів
     */
    public MenuItemRecord(
            String name,
            MenuKind category,
            double price,
            int weightGrams,
            int preparationMinutes) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Назва позиції меню не може бути порожньою");
        }
        if (category == null) {
            throw new IllegalArgumentException("Категорія позиції меню не може бути null");
        }
        if (!Double.isFinite(price) || price < 0.0) {
            throw new IllegalArgumentException("Ціна позиції меню має бути скінченним невід'ємним числом");
        }
        if (weightGrams < 0) {
            throw new IllegalArgumentException("Вага позиції меню не може бути від'ємною");
        }
        if (preparationMinutes < 0) {
            throw new IllegalArgumentException("Час приготування не може бути від'ємним");
        }

        this.name = name;
        this.category = category;
        this.price = price == 0.0 ? 0.0 : price;
        this.weightGrams = weightGrams;
        this.preparationMinutes = preparationMinutes;
    }

    /**
     * Відновлює запис меню з одного CSV-рядка.
     *
     * @param line один рядок CSV у порядку експортованих полів
     * @return відновлений об'єкт {@link MenuItemRecord}
     * @throws DataStorageException якщо формат рядка або значення полів некоректні
     */
    public static MenuItemRecord fromCsv(String line) throws DataStorageException {
        return fromFields(CsvParser.parse(line));
    }

    /**
     * Відновлює запис меню зі списку полів у порядку {@code category, name, preparationMinutes, price, weightGrams}.
     *
     * @param fields список із 5 полів CSV
     * @return відновлений об'єкт {@link MenuItemRecord}
     * @throws DataStorageException якщо кількість полів не дорівнює 5 або значення не проходять розбір чи валідацію
     */
    public static MenuItemRecord fromFields(List<String> fields) throws DataStorageException {
        if (fields == null || fields.size() != 5) {
            int actual = fields == null ? 0 : fields.size();
            throw new DataStorageException(
                    "Очікується 5 полів CSV для MenuItemRecord, отримано: " + actual);
        }
        try {
            MenuKind parsedCategory = MenuKind.valueOf(fields.get(0).trim());
            String parsedName = fields.get(1);
            int parsedPrepMinutes = Integer.parseInt(fields.get(2).trim());
            double parsedPrice = Double.parseDouble(fields.get(3).trim());
            int parsedWeight = Integer.parseInt(fields.get(4).trim());

            return new MenuItemRecord(
                    parsedName,
                    parsedCategory,
                    parsedPrice,
                    parsedWeight,
                    parsedPrepMinutes);
        } catch (IllegalArgumentException exception) {
            throw new DataStorageException(
                    "Некоректні значення полів CSV для MenuItemRecord",
                    exception);
        }
    }

    /**
     * Повертає назву страви.
     *
     * @return назва страви
     */
    public String name() {
        return name;
    }

    /**
     * Повертає категорію страви.
     *
     * @return категорія меню
     */
    public MenuKind category() {
        return category;
    }

    /**
     * Повертає ціну позиції.
     *
     * @return ціна в гривнях
     */
    public double price() {
        return price;
    }

    /**
     * Повертає вагу позиції в грамах.
     *
     * @return вага в грамах
     */
    public int weightGrams() {
        return weightGrams;
    }

    /**
     * Повертає час приготування у хвилинах.
     *
     * @return час приготування у хвилинах
     */
    public int preparationMinutes() {
        return preparationMinutes;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MenuItemRecord that)) {
            return false;
        }
        return Double.compare(price, that.price) == 0
                && weightGrams == that.weightGrams
                && preparationMinutes == that.preparationMinutes
                && name.equals(that.name)
                && category == that.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, category, price, weightGrams, preparationMinutes);
    }

    @Override
    public String toString() {
        return "MenuItemRecord[name=%s, category=%s, price=%.2f, weightGrams=%d, preparationMinutes=%d]"
                .formatted(name, category, price, weightGrams, preparationMinutes);
    }
}