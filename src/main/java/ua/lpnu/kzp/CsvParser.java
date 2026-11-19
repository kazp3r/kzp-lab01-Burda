package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.List;

/**
 * Документний парсер CSV на основі скінченного автомата.
 * Коректно обробляє коми, подвійні лапки та перенесення рядків усередині полів.
 */
public final class CsvParser {

    private CsvParser() {
    }

    /**
     * Розбирає один CSV-запис у список значень полів.
     *
     * @param line один CSV-запис
     * @return незмінний список полів запису
     * @throws DataStorageException якщо рядок порожній, містить кілька записів або має хибний синтаксис
     */
    public static List<String> parse(String line) throws DataStorageException {
        List<List<String>> records = parseDocument(line);
        if (records.size() != 1) {
            throw new DataStorageException("Очікується рівно один CSV-запис");
        }
        return records.get(0);
    }

    /**
     * Розбирає повний текст CSV-документа у список записів.
     *
     * @param text вміст CSV-документа
     * @return незмінний список записів, де кожен запис є незмінним списком полів
     * @throws DataStorageException якщо документ {@code null} або містить некоректні лапки
     */
    public static List<List<String>> parseDocument(String text) throws DataStorageException {
        if (text == null) {
            throw new DataStorageException("CSV-документ не може бути null");
        }
        if (text.isEmpty()) {
            return List.of();
        }

        List<List<String>> records = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        boolean quoteClosed = false;

        for (int index = 0; index < text.length(); index++) {
            char symbol = text.charAt(index);

            if (symbol == '"' && !quoteClosed && !quoted && current.length() == 0) {
                quoted = true;
            } else if (symbol == '"'
                    && quoted
                    && index + 1 < text.length()
                    && text.charAt(index + 1) == '"') {
                current.append('"');
                index++;
            } else if (symbol == '"' && quoted) {
                quoted = false;
                quoteClosed = true;
            } else if (symbol == ',' && !quoted) {
                fields.add(current.toString());
                current.setLength(0);
                quoteClosed = false;
            } else if ((symbol == '\r' || symbol == '\n') && !quoted) {
                fields.add(current.toString());
                records.add(List.copyOf(fields));
                fields = new ArrayList<>();
                current.setLength(0);
                quoteClosed = false;
                if (symbol == '\r'
                        && index + 1 < text.length()
                        && text.charAt(index + 1) == '\n') {
                    index++;
                }
            } else {
                if (quoteClosed) {
                    throw new DataStorageException("Неприпустимий символ після закритих CSV-лапок");
                }
                current.append(symbol);
            }
        }

        if (quoted) {
            throw new DataStorageException("Незакриті лапки в CSV-документі");
        }
        if (!fields.isEmpty() || current.length() > 0) {
            fields.add(current.toString());
            records.add(List.copyOf(fields));
        }

        return List.copyOf(records);
    }
}