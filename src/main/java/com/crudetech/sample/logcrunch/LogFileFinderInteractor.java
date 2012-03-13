package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.FilterChain;
import com.crudetech.sample.filter.MappingIterable;
import com.crudetech.sample.filter.UnaryFunction;

import java.util.Date;

import static java.util.Arrays.asList;

public class LogFileFinderInteractor {
    private final LogFileLocator locator;
    private final FilterChain<StringLogLine> infoFilter;

    public LogFileFinderInteractor(LogFileLocator locator, FilterChain<StringLogLine> infoFilter) {
        this.locator = locator;
        this.infoFilter = infoFilter;
    }

    public Iterable<LogFile> getLogFiles(String name, Date date) {
        return new MappingIterable<LogFile, LogFile>(asList(locator.find(name, date)), filters());
    }

    private UnaryFunction<LogFile, LogFile> filters() {
        return new UnaryFunction<LogFile, LogFile>() {
            @Override
            public LogFile evaluate(LogFile logFile) {
                return new FilterLogFile(logFile, infoFilter);
            }
        };
    }

    private static class FilterLogFile implements LogFile {
        private final LogFile logFile;
        private final FilterChain<StringLogLine> filterChain;

        public FilterLogFile(LogFile logFile, FilterChain<StringLogLine> filterChain) {
            this.logFile = logFile;
            this.filterChain = filterChain;
        }

        @Override
        public Iterable<StringLogLine> getLines() {
            return filterChain.apply(logFile.getLines());
        }

        @Override
        public void close() {
            logFile.close();
        }
    }
}
