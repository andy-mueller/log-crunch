package com.crudetech.sample.logcrunch;

import java.util.ArrayList;
import java.util.Collection;


public abstract class FileSystemFilterLogFileInteractorFactory extends FileSystemLogFileInteractorFactory<FilterLogFileInteractor> {
    @Override
    public FilterLogFileInteractor createInteractor(LogFileLocator locator) {
        return new FilterLogFileInteractor(locator, getFilterBuilder());
    }

    protected Collection<FilterLogFileInteractor.FilterBuilder> getFilterBuilder() {
        return new ArrayList<FilterLogFileInteractor.FilterBuilder>(){{
            add(new FilterLogFileInteractor.LogLevelFilterBuilder());
            add(new FilterLogFileInteractor.SearchIntervalFilterBuilder());
        }};
    }
}
