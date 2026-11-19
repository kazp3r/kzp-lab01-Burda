# Конструювання програмного забезпечення — Проєкт «Меню кафе» (Лабораторна робота № 4)

**Виконав:** ст. гр. КІ-304 Бурда О. В. | **Варіант:** 5 (Меню кафе)

Консольний Java 21 застосунок для зчитування, валідації та потокового аналізу позицій меню кафе.

## Стек та інфраструктура

- **Мова та платформа:** Java 21 (Maven Wrapper `mvnw` / `mvnw.cmd`)
- **Тестування та якість коду:** JUnit 5 (`5.12.2`), SpotBugs (`4.9.7.0`)
- **Пакування та CI:** Maven Shade Plugin (виконуваний JAR), GitHub Actions (`ubuntu-latest`, `windows-latest`, `macos-latest`)

## Структура проєкту

- `MenuItem`, `Dish`, `Drink`, `Dessert`, `MenuKind` — поліморфна модель предметної області з фабричним методом `fromCsv` та розрахунком фінальної вартості `calculateCost()`.
- `MenuReport` — набір чистих потокових запитів (Stream API) без побічних ефектів та розгалужень за підтипами:
  1. `fastPrepItems` (`filter`) — відбір позицій із часом приготування до `20 хв` включно;
  2. `itemNames` (`map`) — перетворення колекції позицій у список назв;
  3. `averagePriceByKind` (`groupingBy` + `averagingDouble`) — обчислення середньої фінальної вартості за категоріями `MenuKind` у `EnumMap`;
  4. `priceStatistics` (`summarizingDouble`) — зведена статистика вартості (`count`, `sum`, `min`, `max`, `average`);
  5. `topItemsByPriceAndPrepTime` / `top5ItemsByPriceAndPrepTime` (`sorted` + `limit`) — вибірка top-N за складеним компаратором: фінальна вартість за спаданням, час приготування за зростанням;
  6. `findByName` (`filter` + `findFirst`) — безпечний пошук першої позиції за назвою з поверненням `Optional<MenuItem>`.
- `MenuController` — координація зчитування CSV-файлу, обробка помилок валідації та формування підсумкового звіту.

## Збірка, перевірка та запуск

```bash
# Запуск модульних тестів
./mvnw test

# Повна перевірка (компіляція, тести, пакування JAR, аналіз SpotBugs)
./mvnw verify

# Запуск зібраного виконуваного JAR
java -jar target/lab01-1.0.0.jar
```

_(У Windows використовуйте `.\mvnw.cmd` замість `./mvnw`)._
