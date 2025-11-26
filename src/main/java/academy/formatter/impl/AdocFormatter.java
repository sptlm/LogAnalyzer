package academy.formatter.impl;

import academy.formatter.OutputFormatter;
import academy.model.DailyStats;
import academy.model.LogAnalysisResult;
import academy.model.Resource;
import academy.model.ResponseCode;
import academy.parser.LogParser;
import academy.util.FormatUtils;
import java.util.List;

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

        return adoc.toString().trim() + "\n";
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
                .append(FormatUtils.formatNumber(result.getTotalRequestsCount()))
                .append("\n");

        // Размеры ответов
        if (result.getResponseSizeInBytes() != null) {
            table.append("| Average Response Size | ")
                    .append(FormatUtils.formatBytes(
                            result.getResponseSizeInBytes().getAverage()))
                    .append("\n");
            table.append("| Max Response Size | ")
                    .append(FormatUtils.formatBytes(
                            result.getResponseSizeInBytes().getMax()))
                    .append("\n");
            table.append("| 95p Response Size | ")
                    .append(FormatUtils.formatBytes(
                            result.getResponseSizeInBytes().getP95()))
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

    private String formatResourcesTable(List<Resource> resources) {
        StringBuilder table = new StringBuilder();
        table.append("[cols=\"70,30\",options=\"header\"]\n");
        table.append("|===\n");
        table.append("| Resource | Count\n");

        for (Resource resource : resources) {
            table.append("| `")
                    .append(escapeAsciidoc(resource.getResource()))
                    .append("` | ")
                    .append(FormatUtils.formatNumber(resource.getTotalRequestsCount()))
                    .append("\n");
        }

        table.append("|===\n");
        return table.toString();
    }

    private String formatResponseCodesTable(List<ResponseCode> responseCodes) {
        StringBuilder table = new StringBuilder();
        table.append("[cols=\"15,40,20\",options=\"header\"]\n");
        table.append("|===\n");
        table.append("| Code | Name | Count\n");

        for (ResponseCode code : responseCodes) {
            String name = LogParser.getStatusName(code.getCode());
            table.append("| ")
                    .append(code.getCode())
                    .append(" | ")
                    .append(name)
                    .append(" | ")
                    .append(FormatUtils.formatNumber(code.getTotalResponsesCount()))
                    .append("\n");
        }

        table.append("|===\n");
        return table.toString();
    }

    private String formatRequestsPerDateTable(List<DailyStats> requestsPerDate) {
        StringBuilder table = new StringBuilder();
        table.append("[cols=\"15,20,20,20\",options=\"header\"]\n");
        table.append("|===\n");
        table.append("| Date | Weekday | Count | Percentage\n");

        for (DailyStats daily : requestsPerDate) {
            table.append("| ")
                    .append(daily.getDate())
                    .append(" | ")
                    .append(daily.getWeekday())
                    .append(" | ")
                    .append(FormatUtils.formatNumber(daily.getTotalRequestsCount()))
                    .append(" | ")
                    .append(String.format("%.2f%%", daily.getTotalRequestsPercentage()))
                    .append("\n");
        }

        table.append("|===\n");
        return table.toString();
    }

    private String escapeAsciidoc(String text) {
        return text.replace("\\", "\\\\").replace("|", "\\|").replace("`", "\\`");
    }
}
