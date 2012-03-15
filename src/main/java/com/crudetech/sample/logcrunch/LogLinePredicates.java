package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.Predicate;

public class LogLinePredicates {
    public static Predicate<LogLine> hasLogLevel(final LogLevel level) {
        return new Predicate<LogLine>() {
            @Override
            public Boolean evaluate(LogLine logLine) {
                return logLine.hasLogLevel(level);
            }
        };
    }

    public static Predicate<LogLine> isInDateTimeRange(final DateTimeRange range) {
        return new Predicate<LogLine>() {
            @Override
            public Boolean evaluate(LogLine argument) {
                return argument.isInRange(range);
            }
        };
    }
}
