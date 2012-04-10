package com.crudetech.sample.logcrunch;

import com.crudetech.sample.logcrunch.logback.LogbackLogLine;

class TestLogLineFactory implements BufferedReaderLogFile.LogLineFactory{
    @Override
    public LogLine newLogLine(String lineContent) {
        return new LogbackLogLine(lineContent, TestLogFile.DateFormat);
    }
}
