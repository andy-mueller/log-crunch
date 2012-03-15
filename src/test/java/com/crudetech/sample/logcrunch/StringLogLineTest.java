package com.crudetech.sample.logcrunch;


import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class StringLogLineTest {

    public static final String LogLineText = "2009-06-07 13:23:57 com.demo.ZeroToFour main INFO: This is an informative message";
    private LogLine line;
    private SimpleDateFormat dateFormat;
    private Date lineDate;

    @Before
    public void setUp() throws Exception {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        lineDate = dateFormat.parse("2009-06-07 13:23:57");
        line = new StringLogLine(LogLineText, dateFormat);
    }

    @Test
    public void ctorParsesInfoLevel() {
        assertThat(line.hasLogLevel(LogLevel.Info), is(true));
    }

    @Test
    public void ctorParsesWarnLevel() {
        line = new StringLogLine("2009-06-07 13:23:57 demo.ZeroToFour main WARN: This is an informative message", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"));
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
        Date start = dateFormat.parse("2009-06-07 13:23:57");
        Date end= dateFormat.parse("2009-06-07 13:28:57");
        DateTimeRange range = spy(new DateTimeRange(start, end));

        assertThat(line.isInRange(range), is(true));
        verify(range).contains(lineDate);
    }

    @Test
    public void hasLogger() throws ParseException {
        Pattern demoLogger = Pattern.compile("com\\.demo\\..*");
        
        assertThat(line.hasLogger(demoLogger), is(true));
    }
}
