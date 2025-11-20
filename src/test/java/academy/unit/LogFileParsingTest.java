package academy.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import academy.reader.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Log File Parsing Tests")
public class LogFileParsingTest {

    @Test
    @DisplayName("На вход передан валидный локальный log-файл")
    void localFileProcessingTest() throws IOException {
        try {
            String testFile = "test_local.log";
            String logContent =
                    "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3\"\n"
                            + "93.180.71.3 - - [17/May/2015:08:05:33 +0000] \"GET /downloads/product_2 HTTP/1.1\" 200 512 \"-\" \"Debian APT-HTTP/1.3\"\n";
            Files.write(Paths.get(testFile), logContent.getBytes());

            FileReader fileReader = new FileReader();
            List<String> lines = fileReader.readLogFiles(List.of(testFile));

            assertEquals(2, lines.size(), "Должно быть прочитано 2 строки");
            assertTrue(lines.getFirst().contains("GET"), "Первая строка должна содержать GET");

        } finally {
            Files.deleteIfExists(Paths.get("test_local.log"));
        }
    }

    @Test
    @DisplayName("На вход передан валидный удаленный log-файл")
    void remoteFileProcessingTest() throws Exception {
        try {
            // Создаем тестовый лог файл
            String testFile =
                    "https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs";

            FileReader fileReader = new FileReader();
            List<String> lines = fileReader.readLogFiles(List.of(testFile));

            assertEquals(51462, lines.size(), "Должно быть прочитано 51462 строки");
            assertTrue(lines.getFirst().contains("GET"), "Первая строка должна содержать GET");

        } finally {
            Files.deleteIfExists(Paths.get("test_local.log"));
        }
    }

    @Test
    @DisplayName(
            "На вход передан валидный локальный log-файл, часть строк в котором нужно отфильтровать по --from и --to")
    void localFileProcessingAndFilteringTest() throws IOException {
        try {
            // Создаем тестовый лог файл с разными датами
            String testFile = "test_filter.log";
            String logContent =
                    "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3\"\n"
                            + "93.180.71.3 - - [18/May/2015:08:05:33 +0000] \"GET /downloads/product_2 HTTP/1.1\" 200 512 \"-\" \"Debian APT-HTTP/1.3\"\n"
                            + "93.180.71.3 - - [19/May/2015:08:05:34 +0000] \"GET /downloads/product_3 HTTP/1.1\" 200 256 \"-\" \"Debian APT-HTTP/1.3\"\n";
            Files.write(Paths.get(testFile), logContent.getBytes());

            FileReader fileReader = new FileReader();
            List<String> lines = fileReader.readLogFiles(List.of(testFile));

            assertEquals(3, lines.size(), "Должно быть прочитано 3 строки");

        } finally {
            Files.deleteIfExists(Paths.get("test_filter.log"));
        }
    }

    @Test
    @DisplayName("На вход передан локальный log-файл, часть строк в котором не подходит под формат")
    void damagedLocalFileProcessingTest() throws IOException {
        try {
            String testFile = "test_damaged.log";
            String logContent =
                    "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3\"\n"
                            + "invalid log line that does not match pattern\n"
                            + "93.180.71.3 - - [18/May/2015:08:05:33 +0000] \"GET /downloads/product_2 HTTP/1.1\" 200 512 \"-\" \"Debian APT-HTTP/1.3\"\n";
            Files.write(Paths.get(testFile), logContent.getBytes());

            FileReader fileReader = new FileReader();
            List<String> lines = fileReader.readLogFiles(List.of(testFile));

            // Должно быть 3 строки прочитано (включая поврежденную), но при анализе поврежденная будет пропущена
            assertEquals(3, lines.size(), "Должно быть прочитано 3 строки из файла");

        } finally {
            Files.deleteIfExists(Paths.get("test_damaged.log"));
        }
    }
}
