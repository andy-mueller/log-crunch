package com.crudetech.sample.logcrunch;

public interface LogFileLocator {
    LogFile find(String fileName, DateTimeRange range);
}
