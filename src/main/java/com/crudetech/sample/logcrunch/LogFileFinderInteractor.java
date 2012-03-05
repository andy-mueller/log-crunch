package com.crudetech.sample.logcrunch;

import java.util.Date;

import static java.util.Arrays.asList;

public class LogFileFinderInteractor {
    private final LogFileLocator locator;

    public LogFileFinderInteractor(LogFileLocator locator) {
        this.locator = locator;
    }

    public Iterable<LogFile> getLogFiles(String name, Date date) {
        return asList(locator.find(name, date));
    }
}
