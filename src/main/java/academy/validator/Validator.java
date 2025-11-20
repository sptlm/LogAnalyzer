package academy.validator;

import academy.model.OutputFormat;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Validator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Validator.class);

    public static OutputFormat validateAndResolveFormat(String format) {
        OutputFormat outputFormat = OutputFormat.fromString(format);
        if (outputFormat == null) {
            LOGGER.error("Unsupported format: {}. Supported formats: {}", format, OutputFormat.getAllFormats());
            throw new IllegalArgumentException(
                    "Unsupported format: " + format + ". Supported formats: " + OutputFormat.getAllFormats());
        }
        return outputFormat;
    }

    public static void validateOutputFile(String output, OutputFormat outputFormat) {
        Path outputPath = Path.of(output);

        // Проверка расширения
        if (!output.toLowerCase().endsWith(outputFormat.getFileExtension())) {
            LOGGER.error("Output file extension does not match format. Expected: {}", outputFormat.getFileExtension());
            throw new IllegalArgumentException(
                    "Output file extension does not match format. Expected: " + outputFormat.getFileExtension());
        }

        // Файл не должен существовать
        if (Files.exists(outputPath)) {
            LOGGER.error("Output file already exists: {}", output);
            throw new IllegalArgumentException("Output file already exists: " + output);
        }

        // Директория должна существовать и быть доступной для записи
        Path dir = outputPath.getParent();
        if (dir != null) {
            if (!Files.exists(dir)) {
                LOGGER.error("Output directory does not exist: {}", dir);
                throw new IllegalArgumentException("Output directory does not exist: " + dir);
            }
            if (!Files.isWritable(dir)) {
                LOGGER.error("Output directory is not writable: {}", dir);
                throw new IllegalArgumentException("Output directory is not writable: " + dir);
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
            LOGGER.error("Invalid ISO8601 date for {}: {}", paramName, value, e);
            throw new IllegalArgumentException("Invalid ISO8601 date for " + paramName + ": " + value, e);
        }
    }

    /** Проверяет, что from <= to (оба параметра могут быть null). */
    public static void validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            LOGGER.error("From date must be before or equal to to date");
            throw new IllegalArgumentException("From date must be before or equal to to date");
        }
    }
}
