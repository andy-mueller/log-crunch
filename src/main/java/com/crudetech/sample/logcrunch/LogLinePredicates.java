package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.Predicate;

public class LogLinePredicates {
    public static Predicate<StringLogLine> hasLogLevel(final LogLevel level) {
        return new Predicate<StringLogLine>() {
            @Override
            public Boolean evaluate(StringLogLine logLine) {
                return logLine.hasLogLevel(level);
            }
        };
    }

    public static Predicate<StringLogLine> isInDateTimeRange(final DateTimeRange range) {
        return new Predicate<StringLogLine>() {
            @Override
            public Boolean evaluate(StringLogLine argument) {
                return argument.isInRange(range);
            }
        };
    }
}
