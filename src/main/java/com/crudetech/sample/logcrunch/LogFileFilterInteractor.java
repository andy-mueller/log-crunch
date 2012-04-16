package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.FilterChain;
import com.crudetech.sample.filter.MappingIterable;
import com.crudetech.sample.filter.PredicateBuilder;
import com.crudetech.sample.filter.UnaryFunction;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.crudetech.sample.logcrunch.LogLinePredicates.hasLogLevel;
import static com.crudetech.sample.logcrunch.LogLinePredicates.isInDateTimeRange;

public class LogFileFilterInteractor {
    private final LogFileLocator locator;

    public static class Query {
        private LogFileNamePattern logFileNamePattern;
        private List<Interval> searchIntervals = new ArrayList<Interval>();
        private List<LogLevel> levels = new ArrayList<LogLevel>();
        private List<Pattern> loggers = new ArrayList<Pattern>();
        private List<Pattern> messageRegex = new ArrayList<Pattern>();

//        @Parameter("logFileNamePattern")
        public void setLogFileNamePattern(LogFileNamePattern logFileNamePattern) {
            this.logFileNamePattern = logFileNamePattern;
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

            Query query = (Query) o;

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
            return "Query{" +
                    "logFileNamePattern=" + logFileNamePattern +
                    ", searchIntervals=" + searchIntervals +
                    ", levels=" + levels +
                    ", loggers=" + loggers +
                    ", messageRegex=" + messageRegex +
                    '}';
        }
    }

    public LogFileFilterInteractor(LogFileLocator locator) {
        this.locator = locator;
    }

    public Iterable<LogFile> getFilteredLogFiles(Query model) {
        Iterable<LogFile> logFiles = locator.find(model.logFileNamePattern, model.searchIntervals);

        FilterChain<LogLine> lineFilter = new FilterChain<LogLine>();
        PredicateBuilder<LogLine> filterBuilder = lineFilter.filterBuilder();

        buildLogLevelFilter(model.levels, filterBuilder);

        buildTimeIntervalFilter(model.searchIntervals, filterBuilder);

        return new MappingIterable<LogFile, LogFile>(logFiles, filterFiles(lineFilter));
    }

    private void buildTimeIntervalFilter(Iterable<Interval> searchIntervals, PredicateBuilder<LogLine> filterBuilder) {
        if (!searchIntervals.iterator().hasNext()) {
            return;
        }
        filterBuilder.andOpenBrace();
        for (Interval searchInterval : searchIntervals) {
            filterBuilder.or(isInDateTimeRange(searchInterval));
        }
        filterBuilder.closeBrace();
    }

    private static void buildLogLevelFilter(Iterable<LogLevel> levels, PredicateBuilder<LogLine> filterBuilder) {
        if (!levels.iterator().hasNext()) {
            return;
        }
        filterBuilder.openBrace();
        for (LogLevel level : levels) {
            filterBuilder.or(hasLogLevel(level));
        }
        filterBuilder.closeBrace();
    }

    private UnaryFunction<LogFile, LogFile> filterFiles(final FilterChain<LogLine> filters) {
        return new UnaryFunction<LogFile, LogFile>() {
            @Override
            public LogFile evaluate(LogFile logFile) {
                return new FilterLogFile(logFile, filters);
            }
        };
    }

    private static class FilterLogFile implements LogFile {
        private final LogFile logFile;
        private final FilterChain<LogLine> filterChain;

        public FilterLogFile(LogFile logFile, FilterChain<LogLine> filterChain) {
            this.logFile = logFile;
            this.filterChain = filterChain;
        }

        @Override
        public Iterable<LogLine> getLines() {
            return filterChain.apply(logFile.getLines());
        }

        @Override
        public void close() {
            logFile.close();
        }
    }
}
