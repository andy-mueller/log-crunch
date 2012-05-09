package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.MappingIterable;
import com.crudetech.sample.filter.UnaryFunction;
import org.joda.time.DateMidnight;
import org.joda.time.Interval;

public class DayWiseLogFileLocator implements LogFileLocator{
    private final LogFileLocator decorated;

    public DayWiseLogFileLocator(LogFileLocator locator) {
        decorated = locator;
    }

    @Override
    public Iterable<LogFile> find(LogFileNamePattern fileNamePattern, Iterable<Interval> ranges) {
        Iterable<Interval> normalizedIntervals = new MappingIterable<Interval, Interval>(ranges, normalizeDayWise());
        return decorated.find(fileNamePattern, normalizedIntervals);
    }

    private UnaryFunction<Interval, Interval> normalizeDayWise() {
        return new UnaryFunction<Interval, Interval>() {
            @Override
            public Interval evaluate(Interval interval) {
                DateMidnight start = new DateMidnight(interval.getStart());
                DateMidnight end = new DateMidnight(interval.getEnd().plusDays(1));
                return new Interval(start, end);
            }
        };
    }
}
