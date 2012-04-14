package com.crudetech.sample.logcrunch;

import com.crudetech.sample.logcrunch.logback.LogbackLogLineFactory;

import java.io.File;
import java.nio.charset.Charset;


public abstract class LogFileFilterInteractorFactory {
    protected File searchPath;
    protected Charset encoding;

    public LogFileFilterInteractorFactory(File searchPath, Charset encoding) {
        this.searchPath = searchPath;
        this.encoding = encoding;
    }

    public LogFileFilterInteractor createInteractor() {
        final BufferedReaderLogFile.LogLineFactory logLineFactory = logLineFactory();

        FileSystemLogFileLocator.LogFileFactory logfileFactory = new FileSystemLogFileLocator.LogFileFactory() {
            @Override
            public LogFile create(File logFile) {
                return new FileLogFile(logFile, logLineFactory, encoding);
            }
        };
        LogFileLocator locator = new FileSystemLogFileLocator(searchPath, logfileFactory);
        return new LogFileFilterInteractor(locator);
    }

    protected abstract LogbackLogLineFactory logLineFactory();
}
