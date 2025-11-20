package academy.validator;

import academy.model.OutputFormat;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Validator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Validator.class);

    /**
     * Валидирует строку формата и возвращает соответствующий OutputFormat. Если формат не поддерживается – кидает
     * IllegalArgumentException.
     */
    public static OutputFormat validateAndResolveFormat(String format) {
        OutputFormat outputFormat = OutputFormat.fromString(format);
        if (outputFormat == null) {
            String msg = "Unsupported format: " + format + ". Supported formats: " + OutputFormat.getAllFormats();
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
        return outputFormat;
    }

    /**
     * Проверяет путь выходного файла: - расширение соответствует формату; - файл ещё не существует; - директория
     * доступна для записи.
     */
    public static void validateOutputFile(String output, OutputFormat outputFormat) {
        Path outputPath = Path.of(output);

        // Проверка расширения
        if (!output.toLowerCase().endsWith(outputFormat.getFileExtension())) {
            String msg = "Output file extension does not match format. Expected: " + outputFormat.getFileExtension();
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Файл не должен существовать
        if (Files.exists(outputPath)) {
            String msg = "Output file already exists: " + output;
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Директория должна существовать и быть доступной для записи
        Path dir = outputPath.getParent();
        if (dir != null) {
            if (!Files.exists(dir)) {
                String msg = "Output directory does not exist: " + dir;
                LOGGER.error(msg);
                throw new IllegalArgumentException(msg);
            }
            if (!Files.isWritable(dir)) {
                String msg = "Output directory is not writable: " + dir;
                LOGGER.error(msg);
                throw new IllegalArgumentException(msg);
            }
        }
    }

    /** Парсит дату в формате ISO8601 (YYYY-MM-DD). */
    public static LocalDate parseIsoDate(String value, String paramName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            String msg = "Invalid ISO8601 date for " + paramName + ": " + value;
            LOGGER.error(msg, e);
            throw new IllegalArgumentException(msg, e);
        }
    }

    /** Проверяет, что from <= to (оба параметра могут быть null). */
    public static void validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            String msg = "From date must be before or equal to to date";
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }
}
