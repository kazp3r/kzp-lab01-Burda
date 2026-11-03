package ua.lpnu.kzp;

/** Категорія позиції меню кафе. */
public enum MenuKind {
    DISH("Страва"),
    DRINK("Напій"),
    DESSERT("Десерт");

    private final String label;

    /* Конструктор enum для створення констант */
    MenuKind(String label) {
        this.label = label;
    }

    /** Повертає текстове подання категорії. */
    public String label() {
        return label;
    }
}