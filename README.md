# Конструювання програмного забезпечення — Проєкт «Меню кафе» (Лабораторна робота № 5)

**Виконав:** ст. гр. КІ-304 Бурда О. В. | **Варіант:** 5 (Меню кафе)

Консольний Java 21 застосунок для зчитування, валідації, потокового аналізу, типізованого зберігання та рефлексивного CSV-експорту позицій меню кафе.

## Стек та інфраструктура

- **Мова та платформа:** Java 21 (Maven Wrapper `mvnw` / `mvnw.cmd`)
- **Тестування та якість коду:** JUnit 5 (`5.12.2`), SpotBugs (`4.9.7.0`)
- **Пакування та CI:** Maven Shade Plugin (виконуваний JAR), GitHub Actions (`ubuntu-latest`, `windows-latest`, `macos-latest`)

## Структура проєкту

- `MenuItem`, `Dish`, `Drink`, `Dessert`, `MenuKind` — поліморфна модель предметної області з фабричним методом `fromCsv` та розрахунком фінальної вартості `calculateCost()`.
- `MenuReport` — набір чистих потокових запитів (Stream API) без побічних ефектів та розгалужень за підтипами (`fastPrepItems`, `itemNames`, `averagePriceByKind`, `priceStatistics`, `topItemsByPriceAndPrepTime`, `findByName`).
- `Repository<T>` — типобезпечне параметризоване сховище в пам'яті з додаванням записів (`add`), незмінним знімком (`all`) та контраваріантною фільтрацією (`find(Predicate<? super T>)`).
- `@CsvColumn`, `CsvExporter`, `CsvParser`, `DataStorageException`, `MenuItemRecord` — підсистема рефлексивного CSV-експорту в кодуванні UTF-8 з екрануванням спецсимволів (ком, лапок, перенесень рядків), скінченним автоматом парсингу та round-trip відновленням об'єктів.
- `MenuController` — координація зчитування вхідного файлу, потокового аналізу, збереження записів у `Repository<MenuItemRecord>`, формування звіту (`out/report.txt`) та експорту CSV (`target/menu-export.csv`).

## Збірка, перевірка та запуск

```bash
# Запуск модульних тестів (17 тестів)
./mvnw test

# Повна перевірка (компіляція, тести, пакування JAR, статичний аналіз SpotBugs)
./mvnw verify

# Запуск зібраного виконуваного JAR у UTF-8
java "-Dfile.encoding=UTF-8" -jar target/lab01-1.0.0.jar
```
