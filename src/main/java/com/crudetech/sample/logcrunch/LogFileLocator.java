package com.crudetech.sample.logcrunch;

import org.joda.time.Interval;

import java.util.List;

public interface LogFileLocator {
    Iterable<LogFile> find(LogFileNamePattern fileName, List<Interval> ranges);
}
