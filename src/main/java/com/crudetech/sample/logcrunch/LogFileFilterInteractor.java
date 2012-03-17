package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.FilterChain;
import com.crudetech.sample.filter.UnaryFunction;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class LogFileFilterInteractor {
    private final LogFileLocator locator;
    private final FilterChain<LogLine> infoFilter;




    public static class RequestModel{
        String logFileName;
        List<Interval> dates = new ArrayList<Interval>();
        List<LogLevel> levels = new ArrayList<LogLevel>();
        List<Pattern> loggers = new ArrayList<Pattern>();
        List<Pattern> messageRegex = new ArrayList<Pattern>();
    }

    public LogFileFilterInteractor(LogFileLocator locator, FilterChain<LogLine> infoFilter) {
        this.locator = locator;
        this.infoFilter = infoFilter;
    }
    public Iterable<LogFile> getFilteredLogFiles(RequestModel model) {
        throw new UnsupportedOperationException("Implement me!");
    }
//    public Iterable<LogFile> getLogFiles(String name, Date date) {
//        DateTimeRange range = new DateTimeRange(date, new Date(date.getTime() + 1));
//        return new MappingIterable<LogFile, LogFile>(asList(locator.find(name, range)), filters());
//    }

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
