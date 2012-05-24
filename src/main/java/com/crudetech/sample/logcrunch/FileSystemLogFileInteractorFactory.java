package com.crudetech.sample.logcrunch;

import java.io.File;
import java.nio.charset.Charset;


public abstract class FileSystemLogFileInteractorFactory<TInteractor> implements InteractorFactory<TInteractor> {
    @Override
    public TInteractor createInteractor() {
        FileSystemLogFileLocator.LogFileFactory logfileFactory = new FileLogFileFactory(logLineFactory(), getEncoding());
        LogFileLocator locator = new DayWiseLogFileLocator(
                new FileSystemLogFileLocator(getSearchPath(), logfileFactory)
                );
        return createInteractor(locator);
    }

    protected abstract TInteractor createInteractor(LogFileLocator locator);

    protected abstract File getSearchPath();

    protected abstract Charset getEncoding();

    protected abstract BufferedReaderLogFile.LogLineFactory logLineFactory();
}
