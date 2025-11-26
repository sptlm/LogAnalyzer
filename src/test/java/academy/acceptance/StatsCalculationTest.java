package academy.acceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import academy.analyzer.LogAnalyzer;
import academy.model.Log;
import academy.model.LogAnalysisResult;
import academy.parser.LogParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Stats Calculation Tests")
public class StatsCalculationTest {
    @Test
    @DisplayName("Расчет статистики на основании локального log-файла")
    void happyPathTest() throws IOException {
        try {
            String testFile = "test_stats.log";
            String logContent =
                    "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 1024 \"-\" \"Debian APT-HTTP/1.3\"\n"
                            + "93.180.71.3 - - [17/May/2015:08:05:33 +0000] \"GET /downloads/product_2 HTTP/1.1\" 200 512 \"-\" \"Debian APT-HTTP/1.3\"\n"
                            + "93.180.71.3 - - [17/May/2015:08:05:34 +0000] \"GET /downloads/product_1 HTTP/1.1\" 200 256 \"-\" \"Debian APT-HTTP/1.3\"\n";
            Files.write(Paths.get(testFile), logContent.getBytes());

            List<Log> lines = LogParser.parseLines(Files.readAllLines(Paths.get(testFile)));
            LogAnalyzer analyzer = new LogAnalyzer();
            LogAnalysisResult result = analyzer.analyze(lines, null, null);

            assertEquals(3, result.getTotalRequestsCount(), "Должно быть 3 запроса");
            assertFalse(result.getResources().isEmpty(), "Должны быть ресурсы");
            assertFalse(result.getResponseCodes().isEmpty(), "Должны быть коды ответа");
            assertNotNull(result.getResponseSizeInBytes(), "Должны быть размеры ответов");
            assertNotNull(result.getResponseSizeInBytes().getAverage(), "Должно быть среднее значение");
            assertNotNull(result.getResponseSizeInBytes().getMax(), "Должно быть максимальное значение");
            assertNotNull(result.getResponseSizeInBytes().getP95(), "Должен быть 95-й перцентиль");

        } finally {
            Files.deleteIfExists(Paths.get("test_stats.log"));
        }
    }
}
