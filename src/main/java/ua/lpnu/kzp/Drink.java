package ua.lpnu.kzp;

/** Напій. */
public final class Drink extends MenuItem {
    
    public Drink(String supplier, String name, double basePrice, int weightG, int prepMin) {
        super(supplier, name, MenuKind.DRINK, basePrice, weightG, prepMin);
    }

    @Override
    public double calculateCost() {
        // Для напоїв додаємо фіксовану вартість тари (наприклад, 5 грн)
        return getPrice() + 5.0;
    }
}