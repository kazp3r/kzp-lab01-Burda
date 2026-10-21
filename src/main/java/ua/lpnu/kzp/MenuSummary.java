package ua.lpnu.kzp;

/**
 * Незмінний набір підсумкових показників для меню кафе.
 *
 * @param count           кількість коректних записів
 * @param averagePrice    середня ціна
 * @param highestWeight   найбільша вага в грамах
 * @param longestPrepTime найдовший час приготування у хвилинах
 */
public record MenuSummary(int count, double averagePrice, int highestWeight, int longestPrepTime) {
    
    /** Перевіряє допустимість підсумкових показників. */
    public MenuSummary {
        if (count < 0 || averagePrice < 0 || highestWeight < 0 || longestPrepTime < 0 
                || !Double.isFinite(averagePrice)) {
            throw new IllegalArgumentException("Показники не можуть бути від'ємними");
        }
    }
}