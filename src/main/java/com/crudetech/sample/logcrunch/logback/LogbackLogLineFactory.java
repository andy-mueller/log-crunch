package com.crudetech.sample.logcrunch.logback;

import com.crudetech.sample.logcrunch.BufferedReaderLogFile;
import com.crudetech.sample.logcrunch.LogLine;

public class LogbackLogLineFactory implements BufferedReaderLogFile.LogLineFactory{
    private final String dateFormat;

    public LogbackLogLineFactory(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public Iterable<LogLine> logLines(Iterable<String> lines) {
        return new LogbackMultipleLineLogLineIterable(lines, dateFormat);
    }
}
