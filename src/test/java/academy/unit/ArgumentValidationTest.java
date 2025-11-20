package academy.unit;

import static org.junit.jupiter.api.Assertions.*;

import academy.Application;
import academy.model.OutputFormat;
import academy.reader.FileReader;
import academy.validator.Validator;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

@DisplayName("Argument Validation Tests")
public class ArgumentValidationTest {

    @Test
    @DisplayName("На вход передан несуществующий локальный файл")
    void test1() {
        FileReader fileReader = new FileReader();
        assertThrows(IllegalArgumentException.class, () -> {
            fileReader.readLogFiles(List.of("/nonexistent/file/path.log"));
        });
    }

    @Test
    @DisplayName("На вход передан несуществующий удаленный файл")
    void test2() {
        FileReader fileReader = new FileReader();
        assertThrows(IllegalArgumentException.class, () -> {
            fileReader.readLogFiles(List.of("https://example.com/nonexistent.log"));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {".docx", ".pdf", ".jpg"})
    @DisplayName("На вход передан файл в неподдерживаемом формате")
    void test3(String extension) {
        FileReader fileReader = new FileReader();
        assertThrows(IllegalArgumentException.class, () -> fileReader.readLogFiles(List.of("test" + extension)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025.01.01 10:30", "today", "01-01-2025", "17/May/2015"})
    @DisplayName("На вход переданы невалидные параметры --from / --to")
    void test4(String from) {
        assertThrows(IllegalArgumentException.class, () -> {
            Validator.parseIsoDate(from, "--from");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Validator.parseIsoDate(from, "--to");
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"txt", "xml", "csv", "html"})
    @DisplayName("Результаты запрошены в неподдерживаемом формате")
    void test5(String format) {
        assertThrows(IllegalArgumentException.class, () -> {
            Validator.validateAndResolveFormat(format);
        });
    }

    @ParameterizedTest
    @MethodSource("test6ArgumentsSource")
    @DisplayName("По пути в аргументе --output указан файл с некоректным расширением")
    void test6(String format, String output) {
        OutputFormat outputFormat = OutputFormat.fromString(format);
        assertNotNull(outputFormat);
        assertThrows(IllegalArgumentException.class, () -> {
            Validator.validateOutputFile(output, outputFormat);
        });
    }

    @Test
    @DisplayName("По пути в аргументе --output уже существует файл")
    void test7() {
        try {
            String testFile = "existing_output.json";
            Files.createFile(Paths.get(testFile));

            OutputFormat outputFormat = OutputFormat.JSON;
            assertThrows(IllegalArgumentException.class, () -> {
                Validator.validateOutputFile(testFile, outputFormat);
            });

        } catch (Exception e) {
            fail("Не удалось создать тестовый файл");
        } finally {
            try {
                Files.deleteIfExists(Paths.get("existing_output.json"));
            } catch (Exception ignored) {
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"--path", "--output", "--format"})
    @DisplayName("На вход не передан обязательный параметр \"{0}\"")
    void test8(String argument) {
        ArrayList<String> argsList = new ArrayList<>();
        for (String param : List.of(new String[] {"--path", "--output", "--format"})) {
            if (!param.equals(argument)) {
                argsList.add(param);
                argsList.add("value");
            }
        }
        String[] args = argsList.toArray(new String[0]);
        Application app = new Application();
        CommandLine cmd = new CommandLine(app);

        assertThrows(CommandLine.MissingParameterException.class, () -> {
            cmd.parseArgs(args);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"--input", "--filter", "--unknown"})
    @DisplayName("На вход передан неподдерживаемый параметр \"{0}\"")
    void test9(String argument) {
        String[] args =
                new String[] {argument, "value", "--path", "test.log", "--format", "json", "--output", "out.json"};
        Application app = new Application();
        CommandLine cmd = new CommandLine(app);

        assertThrows(CommandLine.UnmatchedArgumentException.class, () -> {
            cmd.parseArgs(args);
        });
    }

    @Test
    @DisplayName("Значение параметра --from больше, чем значение параметра --to")
    void test10() {
        java.time.LocalDate from = java.time.LocalDate.of(2025, 12, 31);
        java.time.LocalDate to = java.time.LocalDate.of(2025, 1, 1);

        assertThrows(IllegalArgumentException.class, () -> {
            Validator.validateDateRange(from, to);
        });
    }

    private static Stream<Arguments> test6ArgumentsSource() {
        return Stream.of(
                Arguments.of("markdown", "results.txt"),
                Arguments.of("json", "results.md"),
                Arguments.of("adoc", "results.json"));
    }
}
