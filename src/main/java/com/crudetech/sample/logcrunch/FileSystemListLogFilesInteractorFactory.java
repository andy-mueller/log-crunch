package com.crudetech.sample.logcrunch;

public abstract class FileSystemListLogFilesInteractorFactory extends FileSystemLogFileInteractorFactory<ListLogFilesInteractor> {
    @Override
    public ListLogFilesInteractor createInteractor(LogFileLocator locator) {
        return new ListLogFilesInteractor(locator);
    }
}
