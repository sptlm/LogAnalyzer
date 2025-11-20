package academy.parser;

import academy.model.Log;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogParser.class);

    // Regex для парсинга NGINX лога в формате:
    // '$remote_addr - $remote_user [$time_local] "$request" $status $body_bytes_sent "$http_referer"
    // "$http_user_agent"'
    // Пример: 93.180.71.3 - - [17/May/2015:08:05:32 +0000] "GET /downloads/product_1 HTTP/1.1" 304 0 "-" "Debian
    // APT-HTTP/1.3"
    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^([\\d.]+) - ([^ ]*) \\[([^\\]]+)\\] \"([^\"]+)\" (\\d+) (\\d+) \"([^\"]*)\" \"([^\"]*)\"$");

    public static Log parseLine(String logLine) {
        try {
            Matcher matcher = LOG_PATTERN.matcher(logLine);

            if (!matcher.matches()) {
                LOGGER.warn("Log line does not match expected format: " + logLine);
                return null;
            }

            Log entry = new Log();

            entry.setRemoteAddr(matcher.group(1));
            // group 2 - remote_user (не используется)
            entry.setTimeLocal(matcher.group(3));
            entry.setRequest(matcher.group(4));
            entry.setStatus(Integer.parseInt(matcher.group(5)));
            entry.setBodyBytes(Long.parseLong(matcher.group(6)));
            // group 7 - referer (не используется)
            // group 8 - user_agent (не используется)

            // формат: 17/May/2015:08:05:32 +0000
            // Извлекаем только дату (первые 11 символов: 17/May/2015)
            // Если число однозначное, дописываю 0 в начале и удаляю ":"
            String dateStr = entry.getTimeLocal().substring(0, 11);
            if (dateStr.charAt(10) == ':') {
                dateStr = "0" + dateStr.substring(0, 10);
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy", Locale.ENGLISH);
            entry.setDate(LocalDate.parse(dateStr, formatter));

            // Извлекаем ресурс и протокол из request
            // Формат: METHOD /resource HTTP/VERSION
            String[] parts = entry.getRequest().split(" ");

            if (parts.length >= 3) {
                // parts[0] - метод (не используется)
                entry.setResource(parts[1]); // parts[1] - ресурс
                entry.setProtocol(parts[2]); // parts[2] - протокол и версия (HTTP/1.1)
            } else {
                LOGGER.warn("Invalid request format: " + entry.getRequest());
                entry.setResource("/unknown");
                entry.setProtocol("unknown");
            }

            return entry;

        } catch (Exception e) {
            LOGGER.warn("Error parsing log line: " + logLine + " - " + e.getMessage());
            return null;
        }
    }

    public static String getStatusName(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 304 -> "Not Modified";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            default -> "Unknown";
        };
    }
}
