package academy.model;

import academy.formatter.OutputFormatter;
import academy.formatter.impl.AdocFormatter;
import academy.formatter.impl.JsonFormatter;
import academy.formatter.impl.MarkdownFormatter;

public enum OutputFormat {
    JSON("json", ".json", new JsonFormatter()),
    MARKDOWN("markdown", ".md", new MarkdownFormatter()),
    ADOC("adoc", ".ad", new AdocFormatter());

    private final String format;

    private final String fileExtension;

    private final OutputFormatter outputFormatter;

    OutputFormat(String format, String fileExtension, OutputFormatter outputFormatter) {
        this.format = format;
        this.fileExtension = fileExtension;
        this.outputFormatter = outputFormatter;
    }

    public String getFormat() {
        return format;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public OutputFormatter getOutputFormatter() {
        return outputFormatter;
    }

    public static String getAllFormats() {
        StringBuilder sb = new StringBuilder();
        for (OutputFormat value : OutputFormat.values()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(value.getFormat());
        }
        return sb.toString();
    }

    public static OutputFormat fromString(String format) {
        if (format == null || format.isEmpty()) {
            return null;
        }

        for (OutputFormat outputFormat : OutputFormat.values()) {
            if (outputFormat.format.equalsIgnoreCase(format)) {
                return outputFormat;
            }
        }
        return null;
    }
}
