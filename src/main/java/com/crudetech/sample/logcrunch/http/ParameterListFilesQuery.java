package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.ListLogFilesInteractor;
import com.crudetech.sample.logcrunch.LogFileNamePattern;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;

public class ParameterListFilesQuery implements ListLogFilesInteractor.Query {
    private LogFileNamePattern logFileNamePattern;
    private final List<Interval> searchIntervals = new ArrayList<Interval>();

    @Override
    public LogFileNamePattern getLogFileNamePattern() {
        return logFileNamePattern;
    }

    @Parameter(value = "logFileNamePattern")
    public void setLogFileNamePattern(LogFileNamePattern logFileNamePattern) {
        this.logFileNamePattern = logFileNamePattern;
    }
    @Override
    public Iterable<Interval> getSearchIntervals() {
        return searchIntervals;
    }

    @Parameter("searchRange")
    public void addSearchInterval(Interval searchInterval) {
        searchIntervals.add(searchInterval);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParameterListFilesQuery that = (ParameterListFilesQuery) o;

        return searchIntervals.equals(that.searchIntervals)
            && equals(logFileNamePattern, that.logFileNamePattern);

    }

    private boolean equals(Object lhs, Object rhs) {
        return lhs != null ? lhs.equals(rhs) : lhs == rhs;
    }

    @Override
    public int hashCode() {
        int result = logFileNamePattern != null ? logFileNamePattern.hashCode() : 0;
        result = 31 * result + (searchIntervals != null ? searchIntervals.hashCode() : 0);
        return result;
    }
}
