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
        LogLine info = new StringLogLine(TestLogFile.Line1, TestLogFile.DateFormat);

        Predicate<LogLine> hasLogLevel = LogLinePredicates.hasLogLevel(LogLevel.Info);

        assertThat(hasLogLevel.evaluate(info), is(true));
    }

    @Test
    public void isInDateRange() throws ParseException {
        LogLine info = new StringLogLine(TestLogFile.Line1, TestLogFile.DateFormat);

        DateTime start = TestLogFile.DateFormat.parseDateTime("2009-06-06 13:23:57");
        DateTime end = TestLogFile.DateFormat.parseDateTime("2009-06-08 13:23:57");
        Interval dateRange = new Interval(start, end);
        Predicate<LogLine> isInDateRange = LogLinePredicates.isInDateTimeRange(dateRange);

        assertThat(isInDateRange.evaluate(info), is(true));
    }
}
