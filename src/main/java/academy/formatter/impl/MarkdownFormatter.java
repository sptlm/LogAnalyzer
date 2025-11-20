package academy.formatter.impl;

import academy.formatter.OutputFormatter;
import academy.model.LogAnalysisResult;
import academy.parser.LogParser;
import java.util.List;
import java.util.Map;

/** Форматер для вывода результатов в формате Markdown. Создает таблицы и секции в синтаксисе Markdown. */
public class MarkdownFormatter implements OutputFormatter {

    /**
     * Форматирует результаты анализа в Markdown строку.
     *
     * @param result результаты анализа логов
     * @return Markdown строка с результатами
     */
    @Override
    public String format(LogAnalysisResult result) {
        StringBuilder md = new StringBuilder();

        // Общая информация
        md.append("#### General Information\n\n");
        md.append(formatGeneralInfoTable(result));
        md.append("\n\n");

        // Запрашиваемые ресурсы
        if (!result.getResources().isEmpty()) {
            md.append("#### Requested Resources\n\n");
            md.append(formatResourcesTable(result.getResources()));
            md.append("\n\n");
        }

        // Коды ответа
        if (!result.getResponseCodes().isEmpty()) {
            md.append("#### Response Codes\n\n");
            md.append(formatResponseCodesTable(result.getResponseCodes()));
            md.append("\n\n");
        }

        // Распределение по датам
        if (!result.getRequestsPerDate().isEmpty()) {
            md.append("#### Requests Per Date\n\n");
            md.append(formatRequestsPerDateTable(result.getRequestsPerDate()));
            md.append("\n\n");
        }

        return md.toString().trim();
    }

    /**
     * Форматирует таблицу с общей информацией.
     *
     * @param result результаты анализа
     * @return Markdown таблица
     */
    private String formatGeneralInfoTable(LogAnalysisResult result) {
        StringBuilder table = new StringBuilder();

        table.append("| Metric | Value |\n");
        table.append("|:---------------------:|-------------:|\n");

        // Файлы
        String files = String.join(", ", result.getFiles());
        if (files.length() > 50) {
            files = files.substring(0, 50) + "...";
        }
        table.append("| Files | `").append(files).append("` |\n");

        // Количество запросов
        table.append("| Total Requests | ")
                .append(formatNumber(result.getTotalRequestsCount()))
                .append(" |\n");

        // Размеры ответов
        Map<String, Double> sizes = result.getResponseSizeInBytes();
        if (!sizes.isEmpty()) {
            table.append("| Average Response Size | ")
                    .append(formatBytes(sizes.get("average")))
                    .append(" |\n");
            table.append("| Max Response Size | ")
                    .append(formatBytes(sizes.get("max")))
                    .append(" |\n");
            table.append("| 95p Response Size | ")
                    .append(formatBytes(sizes.get("p95")))
                    .append(" |\n");
        }

        // Уникальные протоколы
        if (!result.getUniqueProtocols().isEmpty()) {
            String protocols = String.join(", ", result.getUniqueProtocols());
            table.append("| Protocols | ").append(protocols).append(" |\n");
        }

        return table.toString();
    }

    private String formatResourcesTable(List<Map<String, Object>> resources) {
        StringBuilder table = new StringBuilder();

        table.append("| Resource | Count |\n");
        table.append("|:---------------:|-----------:|\n");

        for (Map<String, Object> resource : resources) {
            String resourcePath = (String) resource.get("resource");
            int count = (Integer) resource.get("totalRequestsCount");
            table.append("| `")
                    .append(escapeMarkdown(resourcePath))
                    .append("` | ")
                    .append(formatNumber(count))
                    .append(" |\n");
        }

        return table.toString();
    }

    private String formatResponseCodesTable(List<Map<String, Object>> responseCodes) {
        StringBuilder table = new StringBuilder();

        table.append("| Code | Name | Count |\n");
        table.append("|:---:|:---------------------:|-----------:|\n");

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
                    .append(" |\n");
        }

        return table.toString();
    }

    private String formatRequestsPerDateTable(List<Map<String, Object>> requestsPerDate) {
        StringBuilder table = new StringBuilder();

        table.append("| Date | Weekday | Count | Percentage |\n");
        table.append("|:---:|:---:|----------:|----------:|\n");

        for (Map<String, Object> dateEntry : requestsPerDate) {
            String date = (String) dateEntry.get("date");
            String weekday = (String) dateEntry.get("weekday");
            int count = (Integer) dateEntry.get("totalRequestsCount");
            double percentage = (Double) dateEntry.get("totalRequestsPercentage");

            table.append("| ")
                    .append(date)
                    .append(" | ")
                    .append(weekday)
                    .append(" | ")
                    .append(formatNumber(count))
                    .append(" | ")
                    .append(String.format("%.2f%%", percentage))
                    .append(" |\n");
        }

        return table.toString();
    }

    private String formatNumber(int number) {
        return String.format("%,d", number).replace(",", "_");
    }

    private String formatBytes(double bytes) {
        if (bytes < 1024) {
            return String.format("%.0fb", bytes);
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2fkb", bytes / 1024);
        } else {
            return String.format("%.2fmb", bytes / (1024 * 1024));
        }
    }

    private String escapeMarkdown(String text) {
        return text.replace("|", "\\|").replace("[", "\\[").replace("]", "\\]");
    }
}
