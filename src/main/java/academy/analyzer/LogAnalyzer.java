package academy.analyzer;

import academy.model.DailyStats;
import academy.model.Log;
import academy.model.LogAnalysisResult;
import academy.model.Resource;
import academy.model.ResponseCode;
import academy.model.ResponseSizeStats;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

    public LogAnalysisResult analyze(List<Log> logs, LocalDate fromDate, LocalDate toDate) {
        LogAnalysisResult result = new LogAnalysisResult();

        List<Long> responseSizes = new ArrayList<>();
        Map<Integer, Integer> statusCodes = new HashMap<>();
        Map<String, Integer> resources = new HashMap<>();
        Map<LocalDate, Integer> requestsPerDate = new HashMap<>();
        Set<String> protocols = new HashSet<>();

        int totalRequests = 0;
        int skippedLines = 0;

        for (Log entry : logs) {
            if (entry == null) {
                skippedLines++;
                continue;
            }

            if (fromDate != null && entry.getDate().toLocalDate().isBefore(fromDate)) {
                continue;
            }
            if (toDate != null && entry.getDate().toLocalDate().isAfter(toDate)) {
                continue;
            }

            totalRequests++;
            responseSizes.add(entry.getBodyBytes());
            statusCodes.put(entry.getStatus(), statusCodes.getOrDefault(entry.getStatus(), 0) + 1);
            resources.put(entry.getResource(), resources.getOrDefault(entry.getResource(), 0) + 1);
            requestsPerDate.put(
                    entry.getDate().toLocalDate(),
                    requestsPerDate.getOrDefault(entry.getDate().toLocalDate(), 0) + 1);
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

    private ResponseSizeStats calculateResponseSizes(List<Long> responseSizes) {
        ResponseSizeStats stats = new ResponseSizeStats();

        // Среднее значение
        double average =
                responseSizes.stream().mapToLong(Long::longValue).average().orElse(0);
        stats.setAverage(roundToTwoDecimals(average));

        // Максимальное значение
        long max = responseSizes.stream().mapToLong(Long::longValue).max().orElse(0);
        stats.setMax((double) max);

        // 95-й перцентиль
        double p95 = calculatePercentile(responseSizes, 95);
        stats.setP95(roundToTwoDecimals(p95));

        return stats;
    }

    private double calculatePercentile(List<Long> values, int percentile) {
        if (values.isEmpty()) return 0;

        List<Long> sorted = values.stream().sorted().toList();

        double index = percentile / 100.0 * (sorted.size() - 1);
        int lowerIndex = (int) index;
        int upperIndex = lowerIndex + 1;

        if (upperIndex >= sorted.size()) {
            return sorted.get(lowerIndex);
        }

        // Линейная интерполяция между двумя ближайшими значениями
        double fraction = index - lowerIndex;

        return sorted.get(lowerIndex) + fraction * (sorted.get(upperIndex) - sorted.get(lowerIndex));
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private List<ResponseCode> convertStatusCodesToList(Map<Integer, Integer> statusCodes) {
        return statusCodes.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .map(entry -> new ResponseCode(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private List<Resource> getTopResources(Map<String, Integer> resources, int limit) {
        return resources.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new Resource(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private List<DailyStats> getRequestsPerDate(Map<LocalDate, Integer> requestsPerDate, int totalRequests) {

        if (requestsPerDate.isEmpty() || totalRequests == 0) {
            return new ArrayList<>();
        }

        return requestsPerDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    int count = entry.getValue();
                    String weekday = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.of("en", "US"));
                    double percentage = roundToTwoDecimals(count / (double) totalRequests * 100.0);

                    return new DailyStats(date.toString(), weekday, count, percentage);
                })
                .collect(Collectors.toList());
    }
}
