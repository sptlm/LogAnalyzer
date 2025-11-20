package academy.reader;

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

    private static final int INTERNET_TIMEOUT = 10000;

    private final List<String> processedFiles = new ArrayList<>();

    public List<String> getProcessedFiles() {
        return new ArrayList<>(processedFiles);
    }

    public List<String> readLogFiles(List<String> paths) {
        List<String> allLines = new ArrayList<>();

        for (String path : paths) {

            if (path.startsWith("http://") || path.startsWith("https://")) {
                LOGGER.info("Reading remote file: " + path);
                processedFiles.add(path);
                allLines.addAll(readRemoteFile(path));
            } else {
                LOGGER.info("Reading local file(s): " + path);
                allLines.addAll(readLocalFiles(path));
            }
        }

        return allLines;
    }

    private List<String> readLocalFiles(String pathPattern) {
        List<String> allLines = new ArrayList<>();
        List<Path> matchedFiles = new ArrayList<>();

        try {
            // Если это прямой путь к файлу (без подстановочных символов)
            if (!pathPattern.contains("*") && !pathPattern.contains("?")) {
                Path basePath = Path.of(pathPattern);
                if (!Files.exists(basePath)) {
                    throw new IllegalArgumentException("File not found: " + pathPattern);
                }

                if (!isSupportedFileFormat(pathPattern)) {
                    throw new IllegalArgumentException("Unsupported file format: " + pathPattern);
                }

                matchedFiles.add(basePath);
            } else {
                // Есть wildcard: отделяем директорию и шаблон
                int lastSeparator = Math.max(pathPattern.lastIndexOf('/'), pathPattern.lastIndexOf('\\'));

                String dirString;
                String pattern;
                if (lastSeparator >= 0) {
                    dirString = pathPattern.substring(0, lastSeparator);
                    pattern = pathPattern.substring(lastSeparator + 1);
                } else {
                    // шаблон без директории, ищем в текущей
                    dirString = ".";
                    pattern = pathPattern;
                }

                Path dir = Path.of(dirString); // тут уже нет '*'
                if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                    throw new IllegalArgumentException("Directory not found for pattern: " + dirString);
                }

                String regex = patternToRegex(pattern);
                Pattern p = Pattern.compile(regex);

                try (Stream<Path> paths = Files.list(dir)) {
                    paths.filter(path ->
                                    p.matcher(path.getFileName().toString()).matches())
                            .filter(path -> this.isSupportedFileFormat(path.toString()))
                            .forEach(matchedFiles::add);
                }

                if (matchedFiles.isEmpty()) {
                    throw new IllegalArgumentException("No files found matching pattern: " + pathPattern);
                }
            }

            // Читаем содержимое найденных файлов
            for (Path file : matchedFiles) {
                processedFiles.add(file.getFileName().toString());
                LOGGER.info("Reading file: " + file);
                try (Stream<String> lines = Files.lines(file)) {
                    lines.forEach(allLines::add);
                }
            }

        } catch (IOException e) {
            String errMsg = "Error reading local file(s): " + e.getMessage();
            LOGGER.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }

        return allLines;
    }

    private List<String> readRemoteFile(String urlString) {
        List<String> lines = new ArrayList<>();

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(INTERNET_TIMEOUT);
            connection.setReadTimeout(INTERNET_TIMEOUT);

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

            LOGGER.info("Successfully read " + lines.size() + " lines from remote file");
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
