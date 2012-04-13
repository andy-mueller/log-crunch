package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.BufferedReaderLogFile;
import com.crudetech.sample.logcrunch.FileLogFile;
import com.crudetech.sample.logcrunch.FileSystemLogFileLocator;
import com.crudetech.sample.logcrunch.LogFile;
import com.crudetech.sample.logcrunch.LogFileFilterInteractor;
import com.crudetech.sample.logcrunch.LogFileLocator;
import com.crudetech.sample.logcrunch.logback.LogbackLogLineFactory;

import java.io.File;
import java.nio.charset.Charset;

public class LogbackLogFileInteractorFactory {
    private File searchPath;
    private Charset encoding;
    private String logLineFormat;

    public LogbackLogFileInteractorFactory(File searchPath, Charset encoding, String logLineFormat) {
        this.searchPath = searchPath;
        this.encoding = encoding;
        this.logLineFormat= logLineFormat;
    }

    LogFileFilterInteractor createInteractor() {
        final BufferedReaderLogFile.LogLineFactory logLineFactory = new LogbackLogLineFactory(logLineFormat);

        FileSystemLogFileLocator.LogFileFactory logfileFactory = new FileSystemLogFileLocator.LogFileFactory() {
            @Override
            public LogFile create(File logFile) {
                return new FileLogFile(logFile, logLineFactory, encoding);
            }
        };
        LogFileLocator locator = new FileSystemLogFileLocator(searchPath, logfileFactory);
        return new LogFileFilterInteractor(locator);
    }
}
