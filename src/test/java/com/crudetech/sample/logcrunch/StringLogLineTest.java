package com.crudetech.sample.logcrunch;


import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StringLogLineTest {

    private StringLogLine line;
    private SimpleDateFormat dateFormat;

    @Before
    public void setUp() throws Exception {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        line = new StringLogLine("2009-06-07 13:23:57 demo.ZeroToFour main INFO: This is an informative message", dateFormat);
    }

    @Test
    public void ctorParsesInfoLevel() {
        assertThat(line.getLogLevel(), is("INFO"));
    }

    @Test
    public void ctorParsesWarnLevel() {
        line = new StringLogLine("2009-06-07 13:23:57 demo.ZeroToFour main WARN: This is an informative message", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"));
        assertThat(line.getLogLevel(), is("WARN"));
    }

    @Test
    public void ctorParsesDate() throws Exception {
        Date expected = dateFormat.parse("2009-06-07 13:23:57");
        assertThat(line.getDate(), is(expected));
    }
}
