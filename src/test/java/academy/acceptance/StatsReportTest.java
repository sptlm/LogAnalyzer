package academy.acceptance;

import static org.junit.jupiter.api.Assertions.*;

import academy.analyzer.LogAnalyzer;
import academy.formatter.OutputFormatter;
import academy.model.LogAnalysisResult;
import academy.model.OutputFormat;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class StatsReportTest {
    @Test
    @DisplayName("Сохранение статистики в формате JSON")
    void jsonTest() {
        List<String> lines = createTestLogLines();
        LogAnalyzer analyzer = new LogAnalyzer();
        LogAnalysisResult result = analyzer.analyze(lines, null, null);
        result.setFiles(List.of("test.log"));

        OutputFormatter formatter = OutputFormat.JSON.getOutputFormatter();
        String jsonOutput = formatter.format(result);

        assertNotNull(jsonOutput, "JSON не должен быть null");
        assertTrue(jsonOutput.contains("totalRequestsCount"), "JSON должен содержать totalRequestsCount");
        assertTrue(jsonOutput.contains("{"), "JSON должен содержать скобки");
        assertTrue(jsonOutput.contains("}"), "JSON должен содержать скобки");
    }

    @Test
    @DisplayName("Сохранение статистики в формате MARKDOWN")
    void markdownTest() {
        List<String> lines = createTestLogLines();
        LogAnalyzer analyzer = new LogAnalyzer();
        LogAnalysisResult result = analyzer.analyze(lines, null, null);
        result.setFiles(java.util.List.of("test.log"));

        OutputFormatter formatter = OutputFormat.MARKDOWN.getOutputFormatter();
        String markdownOutput = formatter.format(result);

        assertNotNull(markdownOutput, "Markdown не должен быть null");
        assertTrue(markdownOutput.contains("#### General Information"), "Markdown должен содержать заголовок");
        assertTrue(markdownOutput.contains("|"), "Markdown должен содержать таблицы");
    }

    @Test
    @DisplayName("Сохранение статистики в формате ADOC")
    void adocTest() {
        List<String> lines = createTestLogLines();
        LogAnalyzer analyzer = new LogAnalyzer();
        LogAnalysisResult result = analyzer.analyze(lines, null, null);
        result.setFiles(java.util.List.of("test.log"));

        OutputFormatter formatter = OutputFormat.ADOC.getOutputFormatter();
        String adocOutput = formatter.format(result);

        assertNotNull(adocOutput, "AsciiDoc не должен быть null");
        assertTrue(adocOutput.contains("== General Information"), "AsciiDoc должен содержать заголовок");
        assertTrue(adocOutput.contains("|==="), "AsciiDoc должен содержать таблицы");
    }

    private List<String> createTestLogLines() {
        return List.of(
                "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 1024 \"-\" \"Debian APT-HTTP/1.3\"",
                "93.180.71.3 - - [17/May/2015:08:05:33 +0000] \"GET /downloads/product_2 HTTP/1.1\" 200 512 \"-\" \"Debian APT-HTTP/1.3\"",
                "93.180.71.3 - - [17/May/2015:08:05:34 +0000] \"GET /downloads/product_1 HTTP/1.1\" 200 256 \"-\" \"Debian APT-HTTP/1.3\"");
    }
}
