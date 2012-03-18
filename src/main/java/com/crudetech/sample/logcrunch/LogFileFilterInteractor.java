package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.FilterChain;
import com.crudetech.sample.filter.MappingIterable;
import com.crudetech.sample.filter.Predicate;
import com.crudetech.sample.filter.UnaryFunction;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static com.crudetech.sample.logcrunch.LogLinePredicates.hasLogLevel;

public class LogFileFilterInteractor {
    private final LogFileLocator locator;


    public static class RequestModel {
        LogFileNamePattern logFileNamePattern;
        List<Interval> dates = new ArrayList<Interval>();
        List<LogLevel> levels = new ArrayList<LogLevel>();
        List<Pattern> loggers = new ArrayList<Pattern>();
        List<Pattern> messageRegex = new ArrayList<Pattern>();
    }

    public LogFileFilterInteractor(LogFileLocator locator) {
        this.locator = locator;
    }

    public Iterable<LogFile> getFilteredLogFiles(RequestModel model) {
        Iterable<LogFile> logFiles = locator.find(model.logFileNamePattern, model.dates.get(0));

        Collection<Predicate<LogLine>> filterPredicates = new ArrayList<Predicate<LogLine>>();
        for (LogLevel l : model.levels) {
            filterPredicates.add(hasLogLevel(l));
        }
        FilterChain<LogLine> lineFilter = new FilterChain<LogLine>(filterPredicates);

        return new MappingIterable<LogFile, LogFile>(logFiles, filterFiles(lineFilter));
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
