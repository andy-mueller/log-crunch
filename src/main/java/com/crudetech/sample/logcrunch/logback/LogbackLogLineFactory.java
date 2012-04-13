package com.crudetech.sample.logcrunch.logback;

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
    public LogLine newLogLine(String lineContent) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat);
        return new LogbackLogLine(lineContent, formatter);
    }
}
