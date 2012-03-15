package com.crudetech.sample.logcrunch;

class TestLogLineFactory implements BufferedReaderLogFile.LogLineFactory{
    @Override
    public LogLine newLogLine(String lineContent) {
        return new StringLogLine(lineContent, TestLogFile.DateFormat);
    }
}
