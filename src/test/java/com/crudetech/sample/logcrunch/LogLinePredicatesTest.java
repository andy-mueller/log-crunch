package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.Predicate;
import org.junit.Test;

import java.text.ParseException;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LogLinePredicatesTest {
    @Test
    public void hasLogLevel() {
        StringLogLine info = new StringLogLine(TestLogFile.Line1, TestLogFile.DateFormat);

        Predicate<StringLogLine> hasLogLevel = LogLinePredicates.hasLogLevel(LogLevel.Info);

        assertThat(hasLogLevel.evaluate(info), is(true));
    }

    @Test
    public void isInDateRange() throws ParseException {
        StringLogLine info = new StringLogLine(TestLogFile.Line1, TestLogFile.DateFormat);

        Date start = TestLogFile.DateFormat.parse("2009-06-06 13:23:57");
        Date end = TestLogFile.DateFormat.parse("2009-06-08 13:23:57");
        DateTimeRange dateRange = new DateTimeRange(start, end);
        Predicate<StringLogLine> isInDateRange = LogLinePredicates.isInDateTimeRange(dateRange);

        assertThat(isInDateRange.evaluate(info), is(true));
    }
}
