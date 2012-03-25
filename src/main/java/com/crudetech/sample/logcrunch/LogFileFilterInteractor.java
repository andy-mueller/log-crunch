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
        LogFileNamePattern logFileNamePattern;
        List<Interval> searchIntervals = new ArrayList<Interval>();
        List<LogLevel> levels = new ArrayList<LogLevel>();
        List<Pattern> loggers = new ArrayList<Pattern>();
        List<Pattern> messageRegex = new ArrayList<Pattern>();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Query query = (Query) o;

            return levels.equals(query.levels) && searchIntervals.equals(query.searchIntervals);

        }

        @Override
        public int hashCode() {
            int result = searchIntervals != null ? searchIntervals.hashCode() : 0;
            result = 31 * result + (levels != null ? levels.hashCode() : 0);
            return result;
        }
    }

    public LogFileFilterInteractor(LogFileLocator locator) {
        this.locator = locator;
    }

    public Iterable<LogFile> getFilteredLogFiles(Query model) {
        Iterable<LogFile> logFiles = locator.find(model.logFileNamePattern, model.searchIntervals.get(0));

        FilterChain<LogLine> lineFilter = new FilterChain<LogLine>();
        PredicateBuilder<LogLine> filterBuilder = lineFilter.filterBuilder();

        buildLogLevelFilter(model.levels, filterBuilder);

        buildTimeIntervalFilter(model.searchIntervals, filterBuilder);

        return new MappingIterable<LogFile, LogFile>(logFiles, filterFiles(lineFilter));
    }

    private void buildTimeIntervalFilter(List<Interval> searchIntervals, PredicateBuilder<LogLine> filterBuilder) {
        if (!searchIntervals.isEmpty()) {
            filterBuilder.andOpenBrace(isInDateTimeRange(searchIntervals.get(0)));
            for (int i = 1; i < searchIntervals.size(); ++i) {
                filterBuilder.or(isInDateTimeRange(searchIntervals.get(i)));
            }
            filterBuilder.closeBrace();
        }
    }

    private static void buildLogLevelFilter(List<LogLevel> levels, PredicateBuilder<LogLine> filterBuilder) {
        if (levels.isEmpty()) {
            return;
        }
        filterBuilder.openBrace(hasLogLevel(levels.get(0)));
        for (int i = 1; i < levels.size(); ++i) {
            filterBuilder.or(hasLogLevel(levels.get(i)));
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
