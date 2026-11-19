package ua.lpnu.kzp;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Набір чистих потокових запитів (Stream API) для аналізу меню кафе.
 * Не змінює вхідну колекцію та не містить розгалужень за підтипами {@link MenuItem}.
 */
public final class MenuReport {

    /** Поріг швидкого приготування страви у хвилинах (включно). */
    public static final int MAX_FAST_PREP_MIN = 20;

    /** Розмір вибірки за замовчуванням для рейтингу top-N. */
    public static final int DEFAULT_TOP_LIMIT = 5;

    /** Компаратор: фінальна вартість за спаданням, час приготування за зростанням. */
    public static final Comparator<MenuItem> BY_COST_DESC_THEN_PREP_ASC =
            Comparator.comparingDouble(MenuItem::calculateCost)
                    .reversed()
                    .thenComparingInt(MenuItem::getPrepMin);

    private MenuReport() {
    }

    /**
     * Запит 1 (filter): відбирає позиції меню з часом приготування до {@value #MAX_FAST_PREP_MIN} хв включно.
     *
     * @param items список позицій меню
     * @return незмінний список позицій, що готуються не довше {@value #MAX_FAST_PREP_MIN} хв
     */
    public static List<MenuItem> fastPrepItems(List<MenuItem> items) {
        return streamOf(items)
                .filter(item -> item.getPrepMin() <= MAX_FAST_PREP_MIN)
                .toList();
    }

    /**
     * Запит 2 (map): перетворює колекцію позицій меню на список їхніх назв.
     *
     * @param items список позицій меню
     * @return незмінний список назв у порядку потоку
     */
    public static List<String> itemNames(List<MenuItem> items) {
        return streamOf(items)
                .map(MenuItem::getName)
                .toList();
    }

    /**
     * Запит 3 (groupingBy): обчислює середню фінальну вартість позицій для кожної категорії.
     *
     * @param items список позицій меню
     * @return карта категорій {@link MenuKind} та їхньої середньої вартості
     */
    public static Map<MenuKind, Double> averagePriceByKind(List<MenuItem> items) {
        return streamOf(items)
                .collect(Collectors.groupingBy(
                        MenuItem::getKind,
                        () -> new EnumMap<>(MenuKind.class),
                        Collectors.averagingDouble(MenuItem::calculateCost)));
    }

    /**
     * Запит 4 (summarizingDouble): обчислює зведену статистику фінальної вартості позицій.
     *
     * @param items список позицій меню
     * @return об'єкт {@link DoubleSummaryStatistics} (кількість, сума, мінімум, максимум, середнє)
     */
    public static DoubleSummaryStatistics priceStatistics(List<MenuItem> items) {
        return streamOf(items)
                .collect(Collectors.summarizingDouble(MenuItem::calculateCost));
    }

    /**
     * Запит 5 (sorted + limit): повертає перші {@code limit} найдорожчих позицій меню
     * із додатковим впорядкуванням за зростанням часу приготування.
     *
     * @param items список позицій меню
     * @param limit максимальна кількість елементів у результаті (не може бути від'ємною)
     * @return незмінний відсортований список довжиною не більше {@code limit}
     * @throws IllegalArgumentException якщо {@code limit < 0}
     */
    public static List<MenuItem> topItemsByPriceAndPrepTime(List<MenuItem> items, int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Ліміт не може бути від'ємним: " + limit);
        }
        return streamOf(items)
                .sorted(BY_COST_DESC_THEN_PREP_ASC)
                .limit(limit)
                .toList();
    }

    /**
     * Повертає top-5 найдорожчих позицій меню за стандартним правилом сортування.
     *
     * @param items список позицій меню
     * @return незмінний список із не більше ніж 5 позицій
     */
    public static List<MenuItem> top5ItemsByPriceAndPrepTime(List<MenuItem> items) {
        return topItemsByPriceAndPrepTime(items, DEFAULT_TOP_LIMIT);
    }

    /**
     * Запит 6 (Optional): шукає першу позицію меню із вказаною назвою.
     *
     * @param items список позицій меню
     * @param name  назва позиції для пошуку
     * @return {@link Optional} зі знайденою позицією або {@link Optional#empty()}, якщо збігів немає
     */
    public static Optional<MenuItem> findByName(List<MenuItem> items, String name) {
        Objects.requireNonNull(name, "Назва для пошуку не може бути null");
        return streamOf(items)
                .filter(item -> item.getName().equals(name))
                .findFirst();
    }

    /**
     * Повертає найбільшу вагу серед позицій меню або {@code 0}, якщо список порожній.
     *
     * @param items список позицій меню
     * @return максимальна вага у грамах
     */
    public static int maxWeight(List<MenuItem> items) {
        return streamOf(items)
                .mapToInt(MenuItem::getWeightG)
                .max()
                .orElse(0);
    }

    /**
     * Повертає найдовший час приготування серед позицій меню або {@code 0}, якщо список порожній.
     *
     * @param items список позицій меню
     * @return максимальний час приготування у хвилинах
     */
    public static int maxPrepTime(List<MenuItem> items) {
        return streamOf(items)
                .mapToInt(MenuItem::getPrepMin)
                .max()
                .orElse(0);
    }

    private static Stream<MenuItem> streamOf(List<MenuItem> items) {
        return Objects.requireNonNull(items, "Список позицій меню не може бути null").stream();
    }
}