package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.LogFileFilterInteractor;
import com.crudetech.sample.logcrunch.LogFileNamePattern;
import com.crudetech.sample.logcrunch.LogLevel;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ParameterFilterQuery implements LogFileFilterInteractor.FilterQuery {
    private LogFileNamePattern logFileNamePattern;
    private final List<Interval> searchIntervals = new ArrayList<Interval>();
    private final List<LogLevel> levels = new ArrayList<LogLevel>();
    private final List<Pattern> loggers = new ArrayList<Pattern>();
    private final List<Pattern> messageRegex = new ArrayList<Pattern>();

    @Parameter(value = "logFileNamePattern")
    public void setLogFileNamePattern(LogFileNamePattern logFileNamePattern) {
        this.logFileNamePattern = logFileNamePattern;
    }

    @Override
    public LogFileNamePattern getLogFileNamePattern() {
        return logFileNamePattern;
    }

    @Override
    public List<Interval> getSearchIntervals() {
        return searchIntervals;
    }

    @Override
    public List<LogLevel> getLevels() {
        return levels;
    }

    @Override
    public List<Pattern> getLoggers() {
        return loggers;
    }

    @Override
    public List<Pattern> getMessageRegex() {
        return messageRegex;
    }

    @Parameter("searchRange")
    public void addSearchInterval(Interval searchInterval) {
        searchIntervals.add(searchInterval);
    }

    @Parameter(value = "level", required = false)
    public void addLevel(LogLevel level) {
        levels.add(level);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParameterFilterQuery query = (ParameterFilterQuery) o;

        return equals(logFileNamePattern, query.logFileNamePattern)
                && levels.equals(query.levels)
                && searchIntervals.equals(query.searchIntervals);

    }
     private static boolean equals(Object lhs, Object rhs){
         return lhs != null ? lhs.equals(rhs) : lhs == rhs;
     }
    @Override
    public int hashCode() {
        int result = searchIntervals != null ? searchIntervals.hashCode() : 0;
        result = 31 * result + (levels != null ? levels.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FilterQuery{" +
                "logFileNamePattern=" + logFileNamePattern +
                ", searchIntervals=" + searchIntervals +
                ", levels=" + levels +
                ", loggers=" + loggers +
                ", messageRegex=" + messageRegex +
                '}';
    }
}
