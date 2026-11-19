package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Типобезпечне параметризоване сховище об'єктів у пам'яті.
 *
 * @param <T> тип елементів сховища
 */
public final class Repository<T> {

    private final List<T> items = new ArrayList<>();

    /**
     * Додає непорожній запис до сховища.
     *
     * @param item запис для додавання
     * @throws NullPointerException якщо {@code item} є {@code null}
     */
    public void add(T item) {
        items.add(Objects.requireNonNull(item, "Запис не може бути null"));
    }

    /**
     * Повертає незмінний знімок усіх записів сховища.
     *
     * @return незмінний список усіх елементів
     */
    public List<T> all() {
        return List.copyOf(items);
    }

    /**
     * Повертає записи, що задовольняють задану умову пошуку.
     *
     * @param condition предикат фільтрації
     * @return незмінний список знайдених записів
     * @throws NullPointerException якщо {@code condition} є {@code null}
     */
    public List<T> find(Predicate<? super T> condition) {
        Objects.requireNonNull(condition, "Умова пошуку не може бути null");
        return items.stream()
                .filter(condition)
                .toList();
    }
}