package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.FilterChain;

import java.util.Date;

import static java.util.Arrays.asList;

public class LogFileFinderInteractor {
    private final LogFileLocator locator;
    private final FilterChain<StringLogLine> infoFilter;

    public LogFileFinderInteractor(LogFileLocator locator, FilterChain<StringLogLine> infoFilter) {
        this.locator = locator;
        this.infoFilter = infoFilter;
    }

    public Iterable<LogFile> getLogFiles(String name, Date date) {
        return  asList(locator.find(name, date));
    }
}
