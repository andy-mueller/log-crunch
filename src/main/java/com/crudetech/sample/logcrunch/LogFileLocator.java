package com.crudetech.sample.logcrunch;

import org.joda.time.Interval;

public interface LogFileLocator {
    Iterable<LogFile> find(LogFileNamePattern fileName, Iterable<Interval> ranges);
}
