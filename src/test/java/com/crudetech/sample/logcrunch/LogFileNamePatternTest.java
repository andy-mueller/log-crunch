package com.crudetech.sample.logcrunch;

import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LogFileNamePatternTest {
    @Test
    public void matchesName() throws Exception {
        LogFileNamePattern namePattern = new LogFileNamePattern("logFile.%d{yyyy-MM-dd}.log");

        String fileName = "logFile.2009-08-21.log";
        assertThat(namePattern.matches(fileName), is(true));
    }

    @Test
    public void extractsDate() throws Exception {
        LogFileNamePattern namePattern = new LogFileNamePattern("logFile.%d{yyyy-MM-dd}.log");


        DateTime august212009 = new DateTime(2009, 8, 21, 0, 0);

        assertThat(namePattern.dateOf("logFile.2009-08-21.log"), is(august212009));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void patternWithoutDateThrows() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        new LogFileNamePattern("logFile.log");
    }
    @Test
    public void nullInCtorThrows() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        new LogFileNamePattern(null);
    }
    @Test
    public void patternWithMultipleDatesThrows() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        new LogFileNamePattern("logFile.%d{yyyy-MM-dd}.log.%d{yyyy-MM-dd}");
    }
}
