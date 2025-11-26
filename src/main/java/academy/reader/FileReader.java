package academy.reader;

import static academy.validator.Validator.logMsgSanitiser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileReader.class);

    private static final int INTERNET_TIMEOUT_MS = 10000;

    private final List<String> processedFiles = new ArrayList<>();

    public List<String> getProcessedFiles() {
        return processedFiles;
    }

    public List<String> readLogFiles(List<String> paths) {
        List<String> allLines = new ArrayList<>();

        for (String path : paths) {

            if (path.startsWith("http://") || path.startsWith("https://")) {
                LOGGER.info("Reading remote file: {}", logMsgSanitiser(path));
                processedFiles.add(path);
                allLines.addAll(readRemoteFile(path));
            } else {
                LOGGER.info("Reading local file(s): {}", logMsgSanitiser(path));
                allLines.addAll(readLocalFiles(path));
            }
        }

        return allLines;
    }

    // чтение локальных файлов
    private List<String> readLocalFiles(String pathPattern) {
        List<Path> matchedFiles = resolveLocalFiles(pathPattern);
        return readFilesContent(matchedFiles);
    }

    private List<Path> resolveLocalFiles(String pathPattern) {
        List<Path> matchedFiles = new ArrayList<>();

        // Нет подстановок — считаем, что это путь к одному файлу
        if (!hasWildcards(pathPattern)) {
            Path basePath = Path.of(pathPattern);
            validateSingleFilePath(basePath, pathPattern);
            matchedFiles.add(basePath);
            return matchedFiles;
        }

        // Есть wildcard
        Path dir = extractDirectory(pathPattern);
        String pattern = extractPattern(pathPattern);

        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Directory not found for pattern: " + dir.toString());
        }

        String regex = patternToRegex(pattern);
        Pattern compiledPattern = Pattern.compile(regex);

        try (Stream<Path> paths = Files.list(dir)) {
            paths.filter(p -> matchesPattern(p, compiledPattern))
                    .filter(p -> isSupportedFileFormat(p.toString()))
                    .forEach(matchedFiles::add);
        } catch (IOException e) {
            String errMsg = "Error listing directory: " + e.getMessage();
            LOGGER.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }

        if (matchedFiles.isEmpty()) {
            throw new IllegalArgumentException("No files found matching pattern: " + pathPattern);
        }

        return matchedFiles;
    }

    private List<String> readFilesContent(List<Path> files) {
        List<String> allLines = new ArrayList<>();

        for (Path file : files) {
            Path fileName = file.getFileName();
            if (fileName != null) {
                processedFiles.add(fileName.toString());
            }
            LOGGER.info("Reading file: {}", logMsgSanitiser(file.toString()));
            try (Stream<String> lines = Files.lines(file)) {
                lines.forEach(allLines::add);
            } catch (IOException e) {
                String errMsg = "Error reading local file(s): " + e.getMessage();
                LOGGER.error(errMsg, e);
                throw new RuntimeException(errMsg, e);
            }
        }

        return allLines;
    }

    // чтение удаленных файлов
    private List<String> readRemoteFile(String urlString) {
        List<String> lines = new ArrayList<>();

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(INTERNET_TIMEOUT_MS);
            connection.setReadTimeout(INTERNET_TIMEOUT_MS);

            int responseCode = connection.getResponseCode();

            // Проверить код ответа
            if (responseCode == 404) {
                throw new IllegalArgumentException("Remote file not found (404): " + urlString);
            } else if (responseCode != 200) {
                throw new RuntimeException("HTTP error " + responseCode + ": " + urlString);
            }

            // Читать содержимое файла
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }

            LOGGER.info("Successfully read {} lines from remote file", lines.size());
        } catch (IllegalArgumentException e) {
            String errMsg = "Error reading remote file: " + e.getMessage();
            LOGGER.error(errMsg, e);
            throw new IllegalArgumentException(errMsg, e);
        } catch (Exception e) {
            String errMsg = "Error reading remote file: " + e.getMessage();
            LOGGER.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }

        return lines;
    }

    private boolean hasWildcards(String pathPattern) {
        return pathPattern.contains("*") || pathPattern.contains("?");
    }

    private void validateSingleFilePath(Path basePath, String originalPattern) {
        if (!Files.exists(basePath)) {
            throw new IllegalArgumentException("File not found: " + originalPattern);
        }
        if (!isSupportedFileFormat(originalPattern)) {
            throw new IllegalArgumentException("Unsupported file format: " + originalPattern);
        }
    }

    private Path extractDirectory(String pathPattern) {
        int lastSeparator = Math.max(pathPattern.lastIndexOf('/'), pathPattern.lastIndexOf('\\'));
        if (lastSeparator >= 0) {
            String dirString = pathPattern.substring(0, lastSeparator);
            return Path.of(dirString);
        }
        // шаблон без директории, ищем в текущей
        return Path.of(".");
    }

    private String extractPattern(String pathPattern) {
        int lastSeparator = Math.max(pathPattern.lastIndexOf('/'), pathPattern.lastIndexOf('\\'));
        if (lastSeparator >= 0) {
            return pathPattern.substring(lastSeparator + 1);
        }
        return pathPattern;
    }

    private boolean matchesPattern(Path path, Pattern compiledPattern) {
        Path fileName = path.getFileName();
        return fileName != null && compiledPattern.matcher(fileName.toString()).matches();
    }

    private boolean isSupportedFileFormat(String path) {
        String lowerPath = path.toLowerCase();
        return lowerPath.endsWith(".log") || lowerPath.endsWith(".txt");
    }

    private String patternToRegex(String pattern) {
        StringBuilder regex = new StringBuilder("^");
        for (char c : pattern.toCharArray()) {
            if (c == '*') {
                regex.append(".*");
            } else if (c == '?') {
                regex.append(".");
            } else if ("\\[]{}()^$.|+()".indexOf(c) >= 0) {
                regex.append("\\").append(c);
            } else {
                regex.append(c);
            }
        }
        regex.append("$");
        return regex.toString();
    }
}
