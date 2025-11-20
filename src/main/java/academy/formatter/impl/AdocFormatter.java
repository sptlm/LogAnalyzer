package academy.formatter.impl;

import academy.formatter.OutputFormatter;
import academy.model.LogAnalysisResult;
import academy.parser.LogParser;
import java.util.List;
import java.util.Map;
import static academy.util.FormatUtils.formatBytes;
import static academy.util.FormatUtils.formatNumber;

public class AdocFormatter implements OutputFormatter {

    @Override
    public String format(LogAnalysisResult result) {
        StringBuilder adoc = new StringBuilder();

        // Заголовок документа
        adoc.append("= Log Analysis Report\n");
        adoc.append(":doctype: article\n");
        adoc.append(":encoding: utf-8\n\n");

        // Общая информация
        adoc.append("== General Information\n\n");
        adoc.append(formatGeneralInfoTable(result));
        adoc.append("\n\n");

        // Запрашиваемые ресурсы
        if (!result.getResources().isEmpty()) {
            adoc.append("== Requested Resources\n\n");
            adoc.append(formatResourcesTable(result.getResources()));
            adoc.append("\n\n");
        }

        // Коды ответа
        if (!result.getResponseCodes().isEmpty()) {
            adoc.append("== Response Codes\n\n");
            adoc.append(formatResponseCodesTable(result.getResponseCodes()));
            adoc.append("\n\n");
        }

        // Распределение по датам
        if (!result.getRequestsPerDate().isEmpty()) {
            adoc.append("== Requests Per Date\n\n");
            adoc.append(formatRequestsPerDateTable(result.getRequestsPerDate()));
            adoc.append("\n\n");
        }

        return adoc.toString().trim();
    }

    private String formatGeneralInfoTable(LogAnalysisResult result) {
        StringBuilder table = new StringBuilder();

        table.append("[cols=\"30,70\",options=\"header\"]\n");
        table.append("|===\n");
        table.append("| Metric | Value\n");

        // Файлы
        String files = String.join(", ", result.getFiles());
        if (files.length() > 50) {
            files = files.substring(0, 50) + "...";
        }
        table.append("| Files | `").append(escapeAsciidoc(files)).append("`\n");

        // Количество запросов
        table.append("| Total Requests | ")
                .append(formatNumber(result.getTotalRequestsCount()))
                .append("\n");

        // Размеры ответов
        Map<String, Double> sizes = result.getResponseSizeInBytes();
        if (!sizes.isEmpty()) {
            table.append("| Average Response Size | ")
                    .append(formatBytes(sizes.get("average")))
                    .append("\n");
            table.append("| Max Response Size | ")
                    .append(formatBytes(sizes.get("max")))
                    .append("\n");
            table.append("| 95p Response Size | ")
                    .append(formatBytes(sizes.get("p95")))
                    .append("\n");
        }

        // Уникальные протоколы
        if (!result.getUniqueProtocols().isEmpty()) {
            String protocols = String.join(", ", result.getUniqueProtocols());
            table.append("| Protocols | ").append(protocols).append("\n");
        }

        table.append("|===\n");

        return table.toString();
    }

    private String formatResourcesTable(List<Map<String, Object>> resources) {
        StringBuilder table = new StringBuilder();

        table.append("[cols=\"70,30\",options=\"header\"]\n");
        table.append("|===\n");
        table.append("| Resource | Count\n");

        for (Map<String, Object> resource : resources) {
            String resourcePath = (String) resource.get("resource");
            int count = (Integer) resource.get("totalRequestsCount");
            table.append("| `")
                    .append(escapeAsciidoc(resourcePath))
                    .append("` | ")
                    .append(formatNumber(count))
                    .append("\n");
        }

        table.append("|===\n");

        return table.toString();
    }

    private String formatResponseCodesTable(List<Map<String, Object>> responseCodes) {
        StringBuilder table = new StringBuilder();

        table.append("[cols=\"15,40,20\",options=\"header\"]\n");
        table.append("|===\n");
        table.append("| Code | Name | Count\n");

        for (Map<String, Object> codeEntry : responseCodes) {
            int code = (Integer) codeEntry.get("code");
            int count = (Integer) codeEntry.get("totalResponsesCount");
            String name = LogParser.getStatusName(code);
            table.append("| ")
                    .append(code)
                    .append(" | ")
                    .append(name)
                    .append(" | ")
                    .append(formatNumber(count))
                    .append("\n");
        }

        table.append("|===\n");

        return table.toString();
    }

    private String formatRequestsPerDateTable(List<Map<String, Object>> requestsPerDate) {
        StringBuilder table = new StringBuilder();

        table.append("[cols=\"15,20,20,20\",options=\"header\"]\n");
        table.append("|===\n");
        table.append("| Date | Weekday | Count | Percentage\n");

        for (Map<String, Object> dateEntry : requestsPerDate) {
            String date = (String) dateEntry.get("date");
            String dayOfWeek = (String) dateEntry.get("weekday");
            int count = (Integer) dateEntry.get("totalRequestsCount");
            double percentage = (Double) dateEntry.get("totalRequestsPercentage");

            table.append("| ")
                    .append(date)
                    .append(" | ")
                    .append(dayOfWeek)
                    .append(" | ")
                    .append(formatNumber(count))
                    .append(" | ")
                    .append(String.format("%.2f%%", percentage))
                    .append("\n");
        }

        table.append("|===\n");

        return table.toString();
    }

    private String escapeAsciidoc(String text) {
        return text.replace("\\", "\\\\").replace("|", "\\|").replace("`", "\\`");
    }
}
