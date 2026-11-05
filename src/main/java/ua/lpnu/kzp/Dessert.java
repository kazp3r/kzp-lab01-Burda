package ua.lpnu.kzp;

/** Десерт. */
public final class Dessert extends MenuItem {
    
    public Dessert(String supplier, String name, double basePrice, int weightG, int prepMin) {
        super(supplier, name, MenuKind.DESSERT, basePrice, weightG, prepMin);
    }

    @Override
    public double calculateCost() {
        // Для десертів додаємо 10% націнки
        return getPrice() * 1.10;
    }
}