package com.crudetech.sample.logcrunch.logback;

import com.crudetech.sample.filter.MappingIterable;
import com.crudetech.sample.filter.UnaryFunction;
import com.crudetech.sample.logcrunch.BufferedReaderLogFile;
import com.crudetech.sample.logcrunch.LogLine;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class LogbackLogLineFactory implements BufferedReaderLogFile.LogLineFactory{
    private final String dateFormat;

    public LogbackLogLineFactory(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public Iterable<LogLine> logLines(Iterable<String> lines) {
        return new MappingIterable<String, LogLine>(lines, createSingleLine());
    }

    private UnaryFunction<LogLine, String> createSingleLine() {
        final DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat);
        return new UnaryFunction<LogLine, String>() {
            @Override
            public LogLine evaluate(String lineContent) {
                return new LogbackLogLine(lineContent, formatter);
            }
        };
    }
}
