package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Модульні тести для параметризованого сховища, анотації {@link CsvColumn},
 * рефлексивного {@link CsvExporter}, документного {@link CsvParser} та round-trip перевірки.
 */
class StorageAndCsvTest {

    @TempDir
    Path temporaryDirectory;

    /** Сторонній плоский клас для перевірки універсальності {@link CsvExporter}. */
    private static final class SampleDto {

        @CsvColumn("Код")
        private final String code;

        @CsvColumn("Кількість")
        private final int amount;

        private SampleDto(String code, int amount) {
            this.code = code;
            this.amount = amount;
        }
    }

    /** Клас без анотованих полів для перевірки негативного сценарію рефлексії. */
    private static final class UnannotatedDto {
        @SuppressWarnings("unused")
        private final String raw = "test";
    }

    @Test
    void repositoryIsGenericReturnsImmutableSnapshotAndSupportsSuperPredicate() {
        Repository<MenuItemRecord> repository = new Repository<>();
        MenuItemRecord borscht = new MenuItemRecord("Борщ український", MenuKind.DISH, 110.0, 350, 20);
        MenuItemRecord latte = new MenuItemRecord("Лате", MenuKind.DRINK, 65.0, 250, 5);

        repository.add(borscht);
        repository.add(latte);

        List<MenuItemRecord> snapshot = repository.all();
        assertEquals(2, snapshot.size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(borscht));

        Predicate<Object> nonNullObject = obj -> obj.toString().contains("Лате");
        List<MenuItemRecord> found = repository.find(nonNullObject);
        assertEquals(List.of(latte), found);
        assertThrows(NullPointerException.class, () -> repository.add(null));
    }

    @Test
    void csvColumnAnnotationHasRuntimeRetentionAndFieldTarget() {
        Retention retention = CsvColumn.class.getAnnotation(Retention.class);
        Target target = CsvColumn.class.getAnnotation(Target.class);

        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
        assertNotNull(target);
        assertEquals(1, target.value().length);
        assertEquals(ElementType.FIELD, target.value()[0]);
    }

    @Test
    void exportEscapesSpecialCharactersAndRoundTripPreservesFiveMenuRecords() throws Exception {
        Repository<MenuItemRecord> repository = new Repository<>();
        repository.add(new MenuItemRecord("Борщ, домашній з пампушками", MenuKind.DISH, 115.50, 380, 20));
        repository.add(new MenuItemRecord("Кава \"Еспресо\" подвійна", MenuKind.DRINK, 55.00, 60, 3));
        repository.add(new MenuItemRecord("Чай трав'яний\nз м'ятою та медом", MenuKind.DRINK, 60.00, 300, 5));
        repository.add(new MenuItemRecord("Сирники зі сметаною", MenuKind.DESSERT, 95.00, 220, 15));
        repository.add(new MenuItemRecord("Вареники з картоплею", MenuKind.DISH, 88.00, 300, 12));

        Path csvPath = temporaryDirectory.resolve("menu-variant5.csv");
        CsvExporter.write(csvPath, MenuItemRecord.class, repository.all());

        String csvContent = Files.readString(csvPath, StandardCharsets.UTF_8);
        List<List<String>> parsedRecords = CsvParser.parseDocument(csvContent);

        // 1 рядок заголовка + 5 записів (попри 7 фізичних рядків через перенесення всередині лапок)
        assertEquals(6, parsedRecords.size());
        assertEquals("категорія,назва,час готування,ціна,вага", String.join(",", parsedRecords.get(0)));
        assertTrue(csvContent.contains("\"Борщ, домашній з пампушками\""));
        assertTrue(csvContent.contains("\"Кава \"\"Еспресо\"\" подвійна\""));
        assertTrue(csvContent.contains("\"Чай трав'яний\nз м'ятою та медом\""));

        List<MenuItemRecord> restored = parsedRecords.stream()
                .skip(1)
                .map(fields -> {
                    try {
                        return MenuItemRecord.fromFields(fields);
                    } catch (DataStorageException exception) {
                        throw new IllegalStateException(exception);
                    }
                })
                .toList();

        assertEquals(repository.all(), restored);
    }

    @Test
    void exporterWorksWithAnyFlatAnnotatedClass() throws Exception {
        Path csvPath = temporaryDirectory.resolve("sample-dto.csv");
        List<SampleDto> items = List.of(new SampleDto("S-01", 42));

        CsvExporter.write(csvPath, SampleDto.class, items);

        String csv = Files.readString(csvPath, StandardCharsets.UTF_8);
        List<List<String>> rows = CsvParser.parseDocument(csv);
        assertEquals(List.of(List.of("Кількість", "Код"), List.of("42", "S-01")), rows);
    }

    @Test
    void handlesEmptyInputAndRejectsMalformedCsvOrUnannotatedClass() throws Exception {
        Path emptyCsvPath = temporaryDirectory.resolve("empty-menu.csv");
        CsvExporter.write(emptyCsvPath, MenuItemRecord.class, List.of());

        String emptyCsv = Files.readString(emptyCsvPath, StandardCharsets.UTF_8);
        assertEquals(
                List.of(List.of("категорія", "назва", "час готування", "ціна", "вага")),
                CsvParser.parseDocument(emptyCsv));
        assertEquals(List.of(), CsvParser.parseDocument(""));

        // Хибна кількість полів
        assertThrows(DataStorageException.class,
                () -> MenuItemRecord.fromCsv("DISH,Борщ,20"));

        // Невідома категорія enum зі збереженням cause
        DataStorageException badEnumEx = assertThrows(DataStorageException.class,
                () -> MenuItemRecord.fromCsv("INVALID_KIND,Борщ,20,95.0,350"));
        assertTrue(badEnumEx.getCause() instanceof IllegalArgumentException);

        // Некоректне число зі збереженням NumberFormatException у cause
        DataStorageException badNumberEx = assertThrows(DataStorageException.class,
                () -> MenuItemRecord.fromCsv("DISH,Борщ,двадцять,95.0,350"));
        assertTrue(badNumberEx.getCause() instanceof NumberFormatException);

        // Незакриті лапки
        assertThrows(DataStorageException.class,
                () -> CsvParser.parseDocument("\"Незакритий рядок"));

        // Клас без анотованих полів
        Path invalidClassPath = temporaryDirectory.resolve("no-fields.csv");
        assertThrows(DataStorageException.class,
                () -> CsvExporter.write(invalidClassPath, UnannotatedDto.class, List.of(new UnannotatedDto())));
    }

    @Test
    void wrapsFileWriteErrorWithIoExceptionCause() throws Exception {
        Path directoryInsteadOfFile = temporaryDirectory.resolve("existing-directory");
        Files.createDirectory(directoryInsteadOfFile);

        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> CsvExporter.write(directoryInsteadOfFile, MenuItemRecord.class, List.of()));

        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void controllerExportsCsvAlongsideStandardReport() throws Exception {
        Path inputCsv = temporaryDirectory.resolve("input.csv");
        Path reportTxt = temporaryDirectory.resolve("report.txt");
        Path exportedCsv = temporaryDirectory.resolve("exported.csv");

        String inputContent = String.join(System.lineSeparator(),
                "Постачальник А;Борщ;страва;100.0;350;20",
                "Постачальник Б;Американо;напій;50.0;150;5",
                "Постачальник В;;десерт;70.0;120;10");
        Files.writeString(inputCsv, inputContent, StandardCharsets.UTF_8);

        MenuController controller = new MenuController();
        controller.process(inputCsv, reportTxt, exportedCsv);

        String report = Files.readString(reportTxt, StandardCharsets.UTF_8);
        assertTrue(report.contains("Коректних записів: 2"));
        assertTrue(report.contains("Помилок: 1"));

        List<List<String>> rows = CsvParser.parseDocument(Files.readString(exportedCsv, StandardCharsets.UTF_8));
        assertEquals(3, rows.size());
        assertEquals("Борщ", rows.get(1).get(1));
        assertEquals("Американо", rows.get(2).get(1));
    }
}