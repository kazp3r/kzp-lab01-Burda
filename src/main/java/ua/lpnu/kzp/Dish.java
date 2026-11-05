package ua.lpnu.kzp;

/** Страва з основного меню. */
public final class Dish extends MenuItem {
    
    public Dish(String supplier, String name, double basePrice, int weightG, int prepMin) {
        // Передаємо фіксовану категорію DISH до базового класу
        super(supplier, name, MenuKind.DISH, basePrice, weightG, prepMin);
    }

    @Override
    public double calculateCost() {
        // Для страв додаємо 15% за кулінарну обробку
        return getPrice() * 1.15;
    }
}