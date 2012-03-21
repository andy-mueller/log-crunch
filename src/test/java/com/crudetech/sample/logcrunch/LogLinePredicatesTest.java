package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.Predicate;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;

import java.text.ParseException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LogLinePredicatesTest {
    @Test
    public void hasLogLevel() {
        LogLine info = TestLogFile.SampleInfoLine;

        Predicate<LogLine> hasLogLevel = LogLinePredicates.hasLogLevel(LogLevel.Info);

        assertThat(hasLogLevel.evaluate(info), is(true));
    }

    @Test
    public void isInDateRange() throws ParseException {
        LogLine info = TestLogFile.SampleInfoLine;

        DateTime start = TestLogFile.SampleInfoLineDate.minusDays(1);
        DateTime end = TestLogFile.SampleInfoLineDate.plusDays(1);
        Interval dateRange = new Interval(start, end);
        Predicate<LogLine> isInDateRange = LogLinePredicates.isInDateTimeRange(dateRange);

        assertThat(isInDateRange.evaluate(info), is(true));
    }
}
