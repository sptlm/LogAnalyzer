package academy;

import academy.analyzer.LogAnalyzer;
import academy.formatter.OutputFormatter;
import academy.model.LogAnalysisResult;
import academy.model.OutputFormat;
import academy.reader.FileReader;
import academy.validator.Validator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "log-analyzer",
        description = "Analyzes NGINX log files and generates statistics report",
        version = "1.0.0",
        mixinStandardHelpOptions = true)
public class Application implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    @Option(
            names = {"-p", "--path"},
            description = "Path to NGINX log files (local path, pattern or HTTP URL)",
            required = true)
    private List<String> paths;

    @Option(
            names = {"-f", "--format"},
            description = "Output format: json, markdown, adoc",
            required = true)
    private String format;

    @Option(
            names = {"-o", "--output"},
            description = "Output file path",
            required = true)
    private String output;

    @Option(
            names = {"--from"},
            description = "Start date in ISO8601 format (optional), e.g. 2025-03-01")
    private String fromDate;

    @Option(
            names = {"--to"},
            description = "End date in ISO8601 format (optional), e.g. 2025-03-31")
    private String toDate;

    public static void main(String[] args) {
        System.setProperty("line.separator", "\n");

        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        try {
            LOGGER.info("Starting log analysis with format: " + format);

            OutputFormat outputFormat = Validator.validateAndResolveFormat(format);

            Validator.validateOutputFile(output, outputFormat);

            FileReader fileReader = new FileReader();
            List<String> logLines = fileReader.readLogFiles(paths);

            if (logLines.isEmpty()) {
                // Скорее некорректный ввод, может быть выбран не тот файл
                throw new IllegalArgumentException("No log lines were read from files");
            }

            LOGGER.info("Read " + logLines.size() + " lines from log files");

            // Парсинг и валидация дат
            LocalDate parsedFromDate = Validator.parseIsoDate(fromDate, "--from");
            LocalDate parsedToDate = Validator.parseIsoDate(toDate, "--to");
            Validator.validateDateRange(parsedFromDate, parsedToDate);

            // Анализ логов
            LogAnalyzer analyzer = new LogAnalyzer();
            LogAnalysisResult result = analyzer.analyze(logLines, parsedFromDate, parsedToDate);
            result.setFiles(fileReader.getProcessedFiles());

            LOGGER.info("Total requests: " + result.getTotalRequestsCount());

            // Форматирование и запись результата
            OutputFormatter formatter = outputFormat.getOutputFormatter();

            if (formatter == null) {
                throw new RuntimeException("No formatter for format: " + format);
            }

            String formattedResult = formatter.format(result)
                    + System.lineSeparator(); // в expected хотят чтобы пустая строка была в конце :/
            Path outputPath = Path.of(output);
            Files.write(outputPath, formattedResult.getBytes());

            LOGGER.info("Results saved to: " + output);
            System.out.println("Results saved to: " + output);
            return 0;

        } catch (IllegalArgumentException e) {
            LOGGER.error("Validation error: " + e.getMessage(), e);
            System.err.println(e.getMessage());
            return 2;
        } catch (Exception e) {
            String errMsg = "Unexpected error: " + e.getMessage();
            LOGGER.error(errMsg, e);
            System.err.println(errMsg);
            return 1;
        }
    }
}
