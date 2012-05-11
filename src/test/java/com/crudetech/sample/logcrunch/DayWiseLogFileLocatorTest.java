package com.crudetech.sample.logcrunch;

import com.crudetech.sample.Iterables;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DayWiseLogFileLocatorTest {
    @Test
    public void givenSearchIntervalStartingAnyTime_adaptsToFullDays() throws Exception {
        LogFileLocatorStub logFileLocatorStub = new LogFileLocatorStub();
        DayWiseLogFileLocator dayWiseLogFileLocator = new DayWiseLogFileLocator(logFileLocatorStub);

        DateTime sixthOfMayInTheAfternoon = new DateTime(2007, 5, 6, 13, 25);
        DateTime seventhOfMayInTheMorning = new DateTime(2007, 5, 7, 8, 13);
        Interval searchInterval = new Interval(sixthOfMayInTheAfternoon, seventhOfMayInTheMorning);

        dayWiseLogFileLocator.find(anyPattern(), asList(searchInterval));

        DateMidnight expectedStart = new DateMidnight(2007, 5, 6);
        DateMidnight expectedEnd = new DateMidnight(2007, 5, 8);
        Interval expectedRange = new Interval(expectedStart, expectedEnd);

        assertThat(logFileLocatorStub.searchIntervals, is(asList(expectedRange)));
    }

    private LogFileNamePattern anyPattern() {
        return new LogFileNamePattern() {
            @Override
            public boolean matches(String fileName) {
                return false;
            }

            @Override
            public DateTime dateOf(String fileName) {
                return new DateTime(0);
            }
        };
    }

    private static class LogFileLocatorStub implements LogFileLocator {
        List<Interval> searchIntervals;
        LogFileNamePattern pattern;

        @Override
        public Iterable<LogFile> find(LogFileNamePattern pattern , Iterable<Interval> ranges) {
            searchIntervals = Iterables.copy(ranges);
            this.pattern = pattern;
            return asList(logFile);
        }
    }
    private static final LogFile logFile = new LogFile() {
        @Override
        public Iterable<LogLine> getLines() {
            return emptyList();
        }

        @Override
        public void close() {
        }
    };
    @Test
    public void searchPatternIsPassedToDecoratedLocator() throws Exception {
        LogFileLocatorStub logFileLocatorStub = new LogFileLocatorStub();
        DayWiseLogFileLocator dayWiseLogFileLocator = new DayWiseLogFileLocator(logFileLocatorStub);

        LogFileNamePattern pattern = anyPattern();
        dayWiseLogFileLocator.find(pattern, anyTime());

        assertThat(logFileLocatorStub.pattern, is(pattern));
    }

    private Iterable<Interval> anyTime() {
        return emptyList();
    }

    @Test
    public void resultOfDecoratedLocatorIsPassedBackThroughDecorator() throws Exception {
        LogFileLocatorStub logFileLocatorStub = new LogFileLocatorStub();
        DayWiseLogFileLocator dayWiseLogFileLocator = new DayWiseLogFileLocator(logFileLocatorStub);

        LogFileNamePattern pattern = anyPattern();
        Iterable<LogFile> actualFiles = dayWiseLogFileLocator.find(pattern, anyTime());

        assertThat(asList(logFile), is(actualFiles));
    }
}
