package ua.lpnu.kzp;

/**
 * Повідомляє про помилку зберігання, формату CSV, парсингу або рефлексії.
 */
public class DataStorageException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Створює виняток із текстовим описом помилки.
     *
     * @param message опис помилки
     */
    public DataStorageException(String message) {
        super(message);
    }

    /**
     * Створює виняток із текстовим описом помилки та першопричиною.
     *
     * @param message опис помилки
     * @param cause   першопричина винятку
     */
    public DataStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}