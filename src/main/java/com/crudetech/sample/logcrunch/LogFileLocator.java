package com.crudetech.sample.logcrunch;

public interface LogFileLocator {
    Iterable<LogFile> find(LogFileNamePattern fileName, DateTimeRange range);
}
