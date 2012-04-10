package com.crudetech.sample.logcrunch.logback;


import com.crudetech.sample.logcrunch.LogLevel;
import com.crudetech.sample.logcrunch.LogLine;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LogbackLogLineTest {

    public static final String LogLineText = "2009-06-07 13:23:57 com.demo.ZeroToFour main INFO: This is an informative message";
    private LogLine line;
    private DateTimeFormatter dateFormat;
    private DateTime lineDate;

    @Before
    public void setUp() throws Exception {
        dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        lineDate = dateFormat.parseDateTime("2009-06-07 13:23:57");
        line = new LogbackLogLine(LogLineText, dateFormat);
    }

    @Test
    public void ctorParsesInfoLevel() {
        assertThat(line.hasLogLevel(LogLevel.Info), is(true));
    }

    @Test
    public void ctorParsesWarnLevel() {
        line = new LogbackLogLine("2009-06-07 13:23:57 demo.ZeroToFour main WARN: This is an informative message", dateFormat);
        assertThat(line.hasLogLevel(LogLevel.Warn), is(true));
    }

    @Test
    public void ctorParsesDate() throws Exception {
        assertThat(line.hasDate(lineDate), is(true));
    }

    @Test
    public void printWritesOriginalLogLine() throws Exception {
        StringWriter sw = new StringWriter();

        line.print(new PrintWriter(sw));

        assertThat(sw.toString(), is(LogLineText));
    }

    @Test
    public void isDateInRange() throws ParseException {
        DateTime start = dateFormat.parseDateTime("2009-06-07 13:23:57");
        DateTime end= dateFormat.parseDateTime("2009-06-07 13:28:57");
        Interval range = new Interval(start, end);

        assertThat(line.isInRange(range), is(true));
    }
    @Test
    public void isDateNotInRange() throws ParseException {
        DateTime start = dateFormat.parseDateTime("2009-01-07 13:23:57");
        DateTime end= dateFormat.parseDateTime("2009-06-02 13:28:57");
        Interval range = new Interval(start, end);

        assertThat(line.isInRange(range), is(false));
    }
    @Test
    public void hasLogger() throws ParseException {
        Pattern demoLogger = Pattern.compile("com\\.demo\\..*");
        
        assertThat(line.hasLogger(demoLogger), is(true));
    }
}
