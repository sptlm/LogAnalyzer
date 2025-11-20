package academy.formatter.impl;

import academy.formatter.OutputFormatter;
import academy.model.LogAnalysisResult;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;

public class JsonFormatter implements OutputFormatter {

    private final ObjectMapper objectMapper;

    // Кастомный pretty-printer, который управляет отступами и разделителями
    private final DefaultPrettyPrinter prettyPrinter;

    /** Собственный pretty-printer, убирающий пробел перед ":". */
    public static class MyPrettyPrinter extends DefaultPrettyPrinter {

        public MyPrettyPrinter() {
            super();
        }

        // Конструктор копирования – Jackson вызывает createInstance()
        public MyPrettyPrinter(MyPrettyPrinter base) {
            super(base);
        }

        @Override
        public DefaultPrettyPrinter createInstance() {
            return new MyPrettyPrinter(this);
        }

        // Переопределяем разделитель между ключом и значением поля.
        // По умолчанию Jackson пишет " : ", нужно ": ".
        @Override
        public void writeObjectFieldValueSeparator(JsonGenerator g) throws IOException {
            g.writeRaw(": ");
        }
    }

    public JsonFormatter() {
        this.objectMapper = new ObjectMapper();
        // Включаем prettу-print
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Настраиваем отступы: два пробела и перевод строки
        DefaultIndenter indenter = new DefaultIndenter("  ", System.lineSeparator());

        MyPrettyPrinter pp = new MyPrettyPrinter();
        pp.indentObjectsWith(indenter);
        pp.indentArraysWith(indenter);

        this.prettyPrinter = pp;
    }

    @Override
    public String format(LogAnalysisResult result) {
        try {
            // Используем наш кастомный pretty-printer
            return objectMapper.writer(prettyPrinter).writeValueAsString(result);
        } catch (Exception e) {
            throw new RuntimeException("Error formatting result as JSON: " + e.getMessage(), e);
        }
    }
}
