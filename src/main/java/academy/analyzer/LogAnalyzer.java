package academy.analyzer;

import academy.model.Log;
import academy.model.LogAnalysisResult;
import academy.parser.LogParser;
import com.tdunning.math.stats.TDigest;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogAnalyzer.class);

    private static final int TOP_RESOURCES_COUNT = 10;

    private final TDigest tDigest = TDigest.createDigest(100);

    public LogAnalysisResult analyze(List<String> logLines, LocalDate fromDate, LocalDate toDate) {
        LogAnalysisResult result = new LogAnalysisResult();

        List<Long> responseSizes = new ArrayList<>();
        Map<Integer, Integer> statusCodes = new HashMap<>();
        Map<String, Integer> resources = new HashMap<>();
        Map<LocalDate, Integer> requestsPerDate = new HashMap<>();
        Set<String> protocols = new HashSet<>();

        int totalRequests = 0;
        int skippedLines = 0;

        for (String logLine : logLines) {
            Log entry = LogParser.parseLine(logLine);

            if (entry == null) {
                skippedLines++;
                continue;
            }

            if (fromDate != null && entry.getDate().isBefore(fromDate)) {
                continue;
            }
            if (toDate != null && entry.getDate().isAfter(toDate)) {
                continue;
            }

            totalRequests++;
            responseSizes.add(entry.getBodyBytes());
            statusCodes.put(entry.getStatus(), statusCodes.getOrDefault(entry.getStatus(), 0) + 1);
            resources.put(entry.getResource(), resources.getOrDefault(entry.getResource(), 0) + 1);
            requestsPerDate.put(entry.getDate(), requestsPerDate.getOrDefault(entry.getDate(), 0) + 1);
            protocols.add(entry.getProtocol());
        }

        LOGGER.info("Analysis complete: {} requests processed, {} lines skipped", totalRequests, skippedLines);

        result.setTotalRequestsCount(totalRequests);

        if (!responseSizes.isEmpty()) {
            result.setResponseSizeInBytes(calculateResponseSizes(responseSizes));
        }

        result.setResponseCodes(convertStatusCodesToList(statusCodes));

        result.setResources(getTopResources(resources, TOP_RESOURCES_COUNT));

        result.setRequestsPerDate(getRequestsPerDate(requestsPerDate, totalRequests));

        result.setUniqueProtocols(new ArrayList<>(protocols));

        return result;
    }

    private Map<String, Double> calculateResponseSizes(List<Long> responseSizes) {
        Map<String, Double> result = new HashMap<>();

        // среднее значение
        double average =
                responseSizes.stream().mapToLong(Long::longValue).average().orElse(0);
        result.put("average", roundToTwoDecimals(average));

        // максимальное значение
        long max = responseSizes.stream().mapToLong(Long::longValue).max().orElse(0);
        result.put("max", (double) max);

        // 95-й перцентиль
        double p95 = calculatePercentile(responseSizes, 95);
        result.put("p95", roundToTwoDecimals(p95));

        return result;
    }

    private double calculatePercentile(List<Long> values, int percentile) {
        if (values.isEmpty()) return 0;

        List<Long> sorted = values.stream().sorted().toList();

        double index = (percentile / 100.0) * (sorted.size() - 1);
        int lowerIndex = (int) index;
        int upperIndex = lowerIndex + 1;

        if (upperIndex >= sorted.size()) {
            return sorted.get(lowerIndex);
        }

        // Линейная интерполяция между двумя ближайшими значениями
        double fraction = index - lowerIndex;
        System.out.println(fraction);
        System.out.println((sorted.get(upperIndex) - sorted.get(lowerIndex)));

        return sorted.get(lowerIndex) + fraction * (sorted.get(upperIndex) - sorted.get(lowerIndex));
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private List<Map<String, Object>> convertStatusCodesToList(Map<Integer, Integer> statusCodes) {
        return statusCodes.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // Сортировка по количеству
                .map(entry -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("code", entry.getKey());
                    map.put("totalResponsesCount", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getTopResources(Map<String, Integer> resources, int limit) {
        return resources.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // Сортировка по убыванию
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("resource", entry.getKey());
                    map.put("totalRequestsCount", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getRequestsPerDate(Map<LocalDate, Integer> requestsPerDate, int totalRequests) {

        if (requestsPerDate.isEmpty() || totalRequests == 0) {
            return new ArrayList<>();
        }

        return requestsPerDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    LocalDate date = entry.getKey();
                    int count = entry.getValue();

                    map.put("date", date.toString()); // ISO 8601 формат
                    map.put("weekday", date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.of("en", "US")));
                    map.put("totalRequestsCount", count);

                    double percentage = roundToTwoDecimals((count / (double) totalRequests) * 100.0);
                    map.put("totalRequestsPercentage", percentage);

                    return map;
                })
                .collect(Collectors.toList());
    }
}
