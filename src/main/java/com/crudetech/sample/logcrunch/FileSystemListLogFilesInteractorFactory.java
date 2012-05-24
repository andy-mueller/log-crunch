package com.crudetech.sample.logcrunch;

import java.io.File;
import java.nio.charset.Charset;


public abstract class FileSystemListLogFilesInteractorFactory implements InteractorFactory<ListLogFilesInteractor> {
    @Override
    public ListLogFilesInteractor createInteractor() {

        FileSystemLogFileLocator.LogFileFactory logfileFactory = new FileSystemLogFileLocator.LogFileFactory() {
            @Override
            public LogFile create(File logFile) {
                return new FileLogFile(logFile, logLineFactory(), getEncoding());
            }
        };
        LogFileLocator locator = new DayWiseLogFileLocator(
                new FileSystemLogFileLocator(getSearchPath(), logfileFactory)
                );
        return new ListLogFilesInteractor(locator);
    }

    protected abstract File getSearchPath();

    protected abstract Charset getEncoding();

    protected abstract BufferedReaderLogFile.LogLineFactory logLineFactory();
}
