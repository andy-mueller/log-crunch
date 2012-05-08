package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.BinaryFunction;
import com.crudetech.sample.filter.FilterChain;
import com.crudetech.sample.filter.MappingIterable;
import com.crudetech.sample.filter.PredicateBuilder;
import com.crudetech.sample.filter.UnaryFunction;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static com.crudetech.sample.filter.Algorithm.accumulate;
import static com.crudetech.sample.logcrunch.LogLinePredicates.hasLogLevel;
import static com.crudetech.sample.logcrunch.LogLinePredicates.isInDateTimeRange;

public class LogFileFilterInteractor {
    private final LogFileLocator locator;
    private final Iterable<FilterBuilder> filterBuilders;

    public static interface Query {
        LogFileNamePattern getLogFileNamePattern();

        List<Interval> getSearchIntervals();

        List<LogLevel> getLevels();

        List<Pattern> getLoggers();

        List<Pattern> getMessageRegex();
    }

    public static interface LogLineReceiver {
        void receive(LogLine line);
    }

    public static interface FilterBuilder {
        PredicateBuilder<LogLine> build(Query query, PredicateBuilder<LogLine> filterBuilder);
    }

    public LogFileFilterInteractor(LogFileLocator locator, Collection<? extends FilterBuilder> filterBuilders) {
        this.filterBuilders = new ArrayList<FilterBuilder>(filterBuilders);
        this.locator = locator;
    }

    public void getFilteredLines(Query query, LogLineReceiver receiver) {
        for (LogFile filteredLogFile : getFilteredLogFiles(query)) {
            for (LogLine logLine : filteredLogFile.getLines()) {
                receiver.receive(logLine);
            }
            filteredLogFile.close();
        }
    }

    private Iterable<LogFile> getFilteredLogFiles(Query model) {
        Iterable<LogFile> logFiles = locator.find(model.getLogFileNamePattern(), model.getSearchIntervals());

        FilterChain<LogLine> lineFilter = new FilterChain<LogLine>();
        PredicateBuilder<LogLine> filterBuilder = lineFilter.filterBuilder();

        accumulate(filterBuilder, filterBuilders, applyFilterBuilder(model));

        return new MappingIterable<LogFile, LogFile>(logFiles, filterFiles(lineFilter));
    }

    private BinaryFunction<PredicateBuilder<LogLine>, PredicateBuilder<LogLine>, FilterBuilder> applyFilterBuilder(final Query query) {
        return new BinaryFunction<PredicateBuilder<LogLine>, PredicateBuilder<LogLine>, FilterBuilder>() {
            @Override
            public PredicateBuilder<LogLine> evaluate(PredicateBuilder<LogLine> predicateBuilder, FilterBuilder filterBuilder) {
                return filterBuilder.build(query, predicateBuilder);
            }
        };
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

    static class SearchIntervalFilterBuilder implements FilterBuilder {
        @Override
        public PredicateBuilder<LogLine> build(Query query, PredicateBuilder<LogLine> filterBuilder) {
            Iterable<Interval> searchIntervals = query.getSearchIntervals();
            if (!searchIntervals.iterator().hasNext()) {
                return filterBuilder;
            }
            filterBuilder.andOpenBrace();
            for (Interval searchInterval : searchIntervals) {
                filterBuilder.or(isInDateTimeRange(searchInterval));
            }
            filterBuilder.closeBrace();
            return filterBuilder;
        }
    }

    static class LogLevelFilterBuilder implements FilterBuilder {
        @Override
        public PredicateBuilder<LogLine> build(Query query, PredicateBuilder<LogLine> filterBuilder) {
            List<LogLevel> levels = query.getLevels();
            if (!levels.iterator().hasNext()) {
                return filterBuilder;
            }
            filterBuilder.openBrace();
            for (LogLevel level : levels) {
                filterBuilder.or(hasLogLevel(level));
            }
            filterBuilder.closeBrace();
            return filterBuilder;
        }
    }
}
