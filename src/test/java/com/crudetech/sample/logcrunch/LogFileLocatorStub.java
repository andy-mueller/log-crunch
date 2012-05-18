package com.crudetech.sample.logcrunch;

import com.crudetech.sample.Iterables;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

class LogFileLocatorStub implements LogFileLocator {
    List<Interval> searchIntervals;
    LogFileNamePattern pattern;
    final Iterable<LogFile> logFiles;

    LogFileLocatorStub(Collection<? extends LogFile> logFiles) {
        this.logFiles = new ArrayList<LogFile>(logFiles);
    }

    LogFileLocatorStub(LogFile logFile) {
        this(asList(logFile));
    }

    @Override
    public Iterable<LogFile> find(LogFileNamePattern pattern , Iterable<Interval> ranges) {
        searchIntervals = Iterables.copy(ranges);
        this.pattern = pattern;
        return logFiles;
    }
}
