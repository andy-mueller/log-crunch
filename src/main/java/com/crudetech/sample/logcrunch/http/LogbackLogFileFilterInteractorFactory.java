package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.LogFileFilterInteractorFactory;
import com.crudetech.sample.logcrunch.logback.LogbackLogLineFactory;

import java.io.File;
import java.nio.charset.Charset;

public class LogbackLogFileFilterInteractorFactory extends LogFileFilterInteractorFactory {
    private String logLineFormat;

    public LogbackLogFileFilterInteractorFactory(File searchPath, Charset encoding, String logLineFormat) {
        super(searchPath, encoding);
        this.logLineFormat= logLineFormat;
    }

    @Override
    protected LogbackLogLineFactory logLineFactory() {
        return new LogbackLogLineFactory(logLineFormat);
    }
}
