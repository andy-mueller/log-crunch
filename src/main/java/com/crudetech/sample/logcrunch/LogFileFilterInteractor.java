package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.FilterChain;
import com.crudetech.sample.filter.MappingIterable;
import com.crudetech.sample.filter.Predicate;
import com.crudetech.sample.filter.UnaryFunction;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.crudetech.sample.filter.ConcatIterable.concat;
import static com.crudetech.sample.logcrunch.LogLinePredicates.hasLogLevel;
import static com.crudetech.sample.logcrunch.LogLinePredicates.isInDateTimeRange;
import static java.util.Arrays.asList;

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

            if (levels != null ? !levels.equals(query.levels) : query.levels != null) return false;
            if (searchIntervals != null ? !searchIntervals.equals(query.searchIntervals) : query.searchIntervals != null)
                return false;

            return true;
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

        @SuppressWarnings("unchecked")
        Iterable<Predicate<LogLine>> filterPredicates = concat(asList(
                new MappingIterable<LogLevel, Predicate<LogLine>>(model.levels, logLevelFilter()),
                new MappingIterable<Interval, Predicate<LogLine>>(model.searchIntervals, dateRangeFilters())
        ));

        FilterChain<LogLine> lineFilter = new FilterChain<LogLine>(filterPredicates);

        return new MappingIterable<LogFile, LogFile>(logFiles, filterFiles(lineFilter));
    }

    private UnaryFunction<Predicate<LogLine>, Interval> dateRangeFilters() {
        return new UnaryFunction<Predicate<LogLine>, Interval>() {
            @Override
            public Predicate<LogLine> evaluate(Interval argument) {
                return isInDateTimeRange(argument);
            }
        };
    }


    private UnaryFunction<Predicate<LogLine>, LogLevel> logLevelFilter() {
        return new UnaryFunction<Predicate<LogLine>, LogLevel>() {
            @Override
            public Predicate<LogLine> evaluate(LogLevel line) {
                return hasLogLevel(line);
            }
        };
    }


    private UnaryFunction<LogFile, LogFile> filterFiles(final FilterChain<LogLine> infoFilter) {
        return new UnaryFunction<LogFile, LogFile>() {
            @Override
            public LogFile evaluate(LogFile logFile) {
                return new FilterLogFile(logFile, infoFilter);
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
