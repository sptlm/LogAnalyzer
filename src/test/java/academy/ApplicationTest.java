package academy;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

@DisplayName("Application Happy Path Tests")
public class ApplicationTest {
    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Базовая проверка работоспособности программы")
    void happyPathTest() throws Exception {
        Path logFile = tempDir.resolve("test.log");
        String logContent =
                "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /test HTTP/1.1\" 200 1024 \"-\" \"curl\"\n";
        Files.write(logFile, logContent.getBytes());

        Path outputFile = tempDir.resolve("report.json");

        String[] args = {
            "--path", logFile.toString(),
            "--format", "json",
            "--output", outputFile.toString()
        };

        int exitCode = new CommandLine(new academy.Application()).execute(args);

        assertEquals(0, exitCode, "Application should exit with code 0 on success");

        assertTrue(Files.exists(outputFile), "Output file should be created");

        String result = Files.readString(outputFile);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("\"totalRequestsCount\""), "JSON should contain totalRequestsCount field");
    }
}
