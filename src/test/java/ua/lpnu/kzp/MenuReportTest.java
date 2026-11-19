package ua.lpnu.kzp;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Модульні тести для потокових запитів {@link MenuReport}.
 */
class MenuReportTest {

    // Набір із 7 записів: містить значення до/на/після межі 20 хв,
    // дубльовану назву ("Капучино") та однакову ціну для перевірки другого критерію компаратора.
    private final List<MenuItem> sampleMenu = List.of(
            new Drink("Кав'ярня №1", "Капучино", 50.0, 250, 5),         // 1-й дублікат, час < 20
            new Dessert("Пекарня", "Сирник домашній", 80.0, 180, 12),   // однакова базова ціна (80.0), час 12
            new Dish("Кухня", "Борщ український", 120.0, 350, 20),      // межа фільтра: час == 20
            new Dish("Кухня", "Бограч закарпатський", 140.0, 400, 35),  // поза фільтром: час > 20
            new Dessert("Пекарня", "Медівник", 80.0, 170, 8),           // та сама ціна/тип що й Сирник, але час 8 < 12
            new Drink("Кав'ярня №2", "Капучино", 60.0, 300, 6),         // 2-й дублікат назви
            new Drink("Фреш-бар", "Лимонад", 40.0, 300, 3)              // найменша ціна (відсікається у top-5)
    );

    @Test
    void filtersItemsWithPrepTimeUpTo20MinutesIncludingBoundary() {
        List<MenuItem> fastItems = MenuReport.fastPrepItems(sampleMenu);

        // Бограч (35 хв) відфільтровано; Борщ (20 хв) на межі включено
        assertEquals(6, fastItems.size());
        assertTrue(fastItems.stream().allMatch(item -> item.getPrepMin() <= 20));
        assertTrue(fastItems.stream().anyMatch(item -> item.getName().equals("Борщ український")));
    }

    @Test
    void mapsItemsToNamesInStreamOrder() {
        List<String> expectedNames = List.of(
                "Капучино",
                "Сирник домашній",
                "Борщ український",
                "Бограч закарпатський",
                "Медівник",
                "Капучино",
                "Лимонад"
        );
        assertEquals(expectedNames, MenuReport.itemNames(sampleMenu));
    }

    @Test
    void groupsByKindAndCalculatesAverageCost() {
        Map<MenuKind, Double> avgByKind = MenuReport.averagePriceByKind(sampleMenu);

        assertEquals(3, avgByKind.size());
        double expectedDrinkAvg = (sampleMenu.get(0).calculateCost()
                + sampleMenu.get(5).calculateCost()
                + sampleMenu.get(6).calculateCost()) / 3.0;
        double expectedDessertAvg = (sampleMenu.get(1).calculateCost()
                + sampleMenu.get(4).calculateCost()) / 2.0;
        double expectedDishAvg = (sampleMenu.get(2).calculateCost()
                + sampleMenu.get(3).calculateCost()) / 2.0;

        assertEquals(expectedDrinkAvg, avgByKind.get(MenuKind.DRINK), 1e-6);
        assertEquals(expectedDessertAvg, avgByKind.get(MenuKind.DESSERT), 1e-6);
        assertEquals(expectedDishAvg, avgByKind.get(MenuKind.DISH), 1e-6);
    }

    @Test
    void summarizesPricesAndMatchesLegacyLoop() {
        double legacySum = 0.0;
        double legacyMin = Double.POSITIVE_INFINITY;
        double legacyMax = Double.NEGATIVE_INFINITY;
        for (MenuItem item : sampleMenu) {
            double cost = item.calculateCost();
            legacySum += cost;
            legacyMin = Math.min(legacyMin, cost);
            legacyMax = Math.max(legacyMax, cost);
        }

        DoubleSummaryStatistics stats = MenuReport.priceStatistics(sampleMenu);

        assertEquals(sampleMenu.size(), stats.getCount());
        assertEquals(legacySum, stats.getSum(), 1e-6);
        assertEquals(legacyMin, stats.getMin(), 1e-6);
        assertEquals(legacyMax, stats.getMax(), 1e-6);
        assertEquals(legacySum / sampleMenu.size(), stats.getAverage(), 1e-6);
    }

    @Test
    void ordersTop5ByCostDescendingAndPrepTimeAscending() {
        List<MenuItem> top5 = MenuReport.top5ItemsByPriceAndPrepTime(sampleMenu);

        assertEquals(5, top5.size());
        // Медівник (80 грн, 8 хв) має стояти перед Сирником (80 грн, 12 хв)
        int medivnykIdx = top5.indexOf(sampleMenu.get(4));
        int syrnykIdx = top5.indexOf(sampleMenu.get(1));
        assertTrue(medivnykIdx >= 0 && syrnykIdx >= 0 && medivnykIdx < syrnykIdx);

        // Медівник має йти перед Сирником
        assertEquals("Медівник", top5.get(medivnykIdx).getName());
        assertEquals("Сирник домашній", top5.get(syrnykIdx).getName());
    }

    @Test
    void handlesTopNEdgeCases() {
        assertEquals(List.of(), MenuReport.topItemsByPriceAndPrepTime(sampleMenu, 0));
        assertEquals(sampleMenu.size(), MenuReport.topItemsByPriceAndPrepTime(sampleMenu, 50).size());
        assertThrows(IllegalArgumentException.class,
                () -> MenuReport.topItemsByPriceAndPrepTime(sampleMenu, -1));
    }

    @Test
    void findsFirstMatchForDuplicateAndReturnsEmptyWhenMissing() {
        Optional<MenuItem> found = MenuReport.findByName(sampleMenu, "Капучино");
        assertTrue(found.isPresent());
        assertEquals("Кав'ярня №1", found.get().getSupplier());

        Optional<MenuItem> missing = MenuReport.findByName(sampleMenu, "Неіснуюча страва");
        assertTrue(missing.isEmpty());
    }

    @Test
    void handlesEmptyCollectionGracefully() {
        List<MenuItem> empty = List.of();

        assertTrue(MenuReport.fastPrepItems(empty).isEmpty());
        assertTrue(MenuReport.itemNames(empty).isEmpty());
        assertTrue(MenuReport.averagePriceByKind(empty).isEmpty());
        assertTrue(MenuReport.top5ItemsByPriceAndPrepTime(empty).isEmpty());
        assertTrue(MenuReport.findByName(empty, "Капучино").isEmpty());

        DoubleSummaryStatistics emptyStats = MenuReport.priceStatistics(empty);
        assertEquals(0, emptyStats.getCount());
        assertEquals(0.0, emptyStats.getSum());
        assertEquals(0.0, emptyStats.getAverage());
        assertEquals(0, MenuReport.maxWeight(empty));
        assertEquals(0, MenuReport.maxPrepTime(empty));
    }

    @Test
    void queriesDoNotMutateInputList() {
        List<MenuItem> snapshot = List.copyOf(sampleMenu);

        MenuReport.fastPrepItems(sampleMenu);
        MenuReport.itemNames(sampleMenu);
        MenuReport.averagePriceByKind(sampleMenu);
        MenuReport.priceStatistics(sampleMenu);
        MenuReport.top5ItemsByPriceAndPrepTime(sampleMenu);
        MenuReport.findByName(sampleMenu, "Капучино");

        assertEquals(snapshot, sampleMenu);
    }
}