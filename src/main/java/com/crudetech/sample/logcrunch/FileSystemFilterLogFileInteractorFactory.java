package com.crudetech.sample.logcrunch;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;


public abstract class FileSystemFilterLogFileInteractorFactory implements FilterLogFileInteractorFactory {
    @Override
    public FilterLogFileInteractor createInteractor() {

        FileSystemLogFileLocator.LogFileFactory logfileFactory = new FileSystemLogFileLocator.LogFileFactory() {
            @Override
            public LogFile create(File logFile) {
                return new FileLogFile(logFile, logLineFactory(), getEncoding());
            }
        };
        LogFileLocator locator = new DayWiseLogFileLocator(
                new FileSystemLogFileLocator(getSearchPath(), logfileFactory)
                );
        return new FilterLogFileInteractor(locator, getFilterBuilder());
    }

    protected Collection<FilterLogFileInteractor.FilterBuilder> getFilterBuilder() {
        return new ArrayList<FilterLogFileInteractor.FilterBuilder>(){{
            add(new FilterLogFileInteractor.LogLevelFilterBuilder());
            add(new FilterLogFileInteractor.SearchIntervalFilterBuilder());
        }};
    }

    protected abstract File getSearchPath();

    protected abstract Charset getEncoding();

    protected abstract BufferedReaderLogFile.LogLineFactory logLineFactory();
}
