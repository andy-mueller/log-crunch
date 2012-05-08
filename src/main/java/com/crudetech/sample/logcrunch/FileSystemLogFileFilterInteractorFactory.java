package com.crudetech.sample.logcrunch;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;


public abstract class FileSystemLogFileFilterInteractorFactory implements LogFileFilterInteractorFactory {
    @Override
    public LogFileFilterInteractor createInteractor() {

        FileSystemLogFileLocator.LogFileFactory logfileFactory = new FileSystemLogFileLocator.LogFileFactory() {
            @Override
            public LogFile create(File logFile) {
                return new FileLogFile(logFile, logLineFactory(), getEncoding());
            }
        };
        LogFileLocator locator = new FileSystemLogFileLocator(getSearchPath(), logfileFactory);
        return new LogFileFilterInteractor(locator, getFilterBuilder());
    }

    protected Collection<LogFileFilterInteractor.FilterBuilder> getFilterBuilder() {
        return new ArrayList<LogFileFilterInteractor.FilterBuilder>(){{
            add(new LogFileFilterInteractor.LogLevelFilterBuilder());
            add(new LogFileFilterInteractor.SearchIntervalFilterBuilder());
        }};
    }

    protected abstract File getSearchPath();

    protected abstract Charset getEncoding();

    protected abstract BufferedReaderLogFile.LogLineFactory logLineFactory();
}
