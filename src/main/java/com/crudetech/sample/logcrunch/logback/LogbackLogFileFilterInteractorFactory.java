package com.crudetech.sample.logcrunch.logback;

import com.crudetech.sample.logcrunch.BufferedReaderLogFile;
import com.crudetech.sample.logcrunch.LogFileFilterInteractorFactory;

import java.io.File;
import java.nio.charset.Charset;

public class LogbackLogFileFilterInteractorFactory extends LogFileFilterInteractorFactory {
    private String logLineFormat;

    private File searchPath;
    private Charset encoding;


    public LogbackLogFileFilterInteractorFactory(File searchPath, Charset encoding, String logLineFormat) {
        this.searchPath = searchPath;
        this.encoding = encoding;
        this.logLineFormat = logLineFormat;
    }

    @Override
    protected File getSearchPath() {
        return searchPath;
    }

    @Override
    protected Charset getEncoding() {
        return encoding;
    }

    @Override
    protected BufferedReaderLogFile.LogLineFactory logLineFactory() {
        return new LogbackLogLineFactory(logLineFormat);
    }
}
