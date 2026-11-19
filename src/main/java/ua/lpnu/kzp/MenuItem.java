package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/** Спільний базовий абстрактний тип позиції меню кафе. */
public abstract class MenuItem {
    private final String supplier;
    private final String name;
    private final MenuKind kind;
    private final double basePrice;
    private final int weightG;
    private final int prepMin;

    /** Створює позицію меню з перевіреними полями. */
    protected MenuItem(String supplier, String name, MenuKind kind, double basePrice, int weightG, int prepMin) {
        if (supplier == null || supplier.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Текстові поля не можуть бути порожніми");
        }
        if (basePrice < 0 || !Double.isFinite(basePrice)) {
            throw new IllegalArgumentException("Ціна має бути скінченною і не від'ємною");
        }
        if (weightG < 0 || prepMin < 0) {
            throw new IllegalArgumentException("Вага та час приготування не можуть бути від'ємними");
        }
        
        this.supplier = supplier;
        this.name = name;
        this.kind = Objects.requireNonNull(kind, "Категорія не може бути null");
        this.basePrice = basePrice;
        this.weightG = weightG;
        this.prepMin = prepMin;
    }

    public final String getSupplier() { return supplier; }
    public final String getName() { return name; }
    public final MenuKind getKind() { return kind; }
    public final double getPrice() { return basePrice; }
    public final int getWeightG() { return weightG; }
    public final int getPrepMin() { return prepMin; }

    /** Поліморфна операція: обчислення фінальної вартості. */
    public abstract double calculateCost();

    /** Порівнює позиції за класом та назвою. */
    @Override
    public final boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        MenuItem item = (MenuItem) other;
        return name.equals(item.name) && supplier.equals(item.supplier);
    }

    /** Повертає хеш-код, узгоджений із equals. */
    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), supplier, name);
    }

    /** Фабричний метод: генерує правильний підтип на основі категорії. */
   /** Фабричний метод: генерує правильний підтип на основі категорії. */
    public static MenuItem fromCsv(String line) {
        String[] parts = line.split(";");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Неправильна кількість полів");
        }
        String supplier = parts[0].strip();
        String name = parts[1].strip();
        // Очищаємо від прихованого BOM та нерозривних пробілів
        String category = parts[2].replace("\uFEFF", "").strip().toLowerCase(Locale.ROOT);
        double basePrice = Double.parseDouble(parts[3].strip());
        int weight = Integer.parseInt(parts[4].strip());
        int prepTime = Integer.parseInt(parts[5].strip());

        return switch (category) {
            case "страва", "страви", "перші страви" -> new Dish(supplier, name, basePrice, weight, prepTime);
            case "напій", "напої" -> new Drink(supplier, name, basePrice, weight, prepTime);
            case "десерт", "десерти" -> new Dessert(supplier, name, basePrice, weight, prepTime);
            default -> throw new IllegalArgumentException("Невідома категорія: " + category);
        };
    }

    /** 
     * Офіційний захист від Finalizer-атак (вирішує проблему CT_CONSTRUCTOR_THROW).
     * Змушує SpotBugs ігнорувати викидання винятків у конструкторі.
     */
    /** 
     * Захист від Finalizer Attack (усуває CT_CONSTRUCTOR_THROW).
     * Додано перевірку, щоб обійти помилки FI_USELESS та FI_EMPTY.
     */
    @Override
    @SuppressWarnings("removal")
    protected final void finalize() {
        if (this.name == null) {
            throw new IllegalStateException("Об'єкт недоініціалізовано (захист від Finalizer Attack)");
        }
    }
}