package academy.formatter;

import academy.model.LogAnalysisResult;

public interface OutputFormatter {

    String format(LogAnalysisResult result);
}
