package com.crudetech.sample.logcrunch;


import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StringLogLineTest {

    private StringLogLine line;

    @Before
    public void setUp() throws Exception {
        line = new StringLogLine("2009-06-07 13:23:57 demo.ZeroToFour main INFO: This is an informative message");
    }

    @Test
    public void ctorParsesInfoLevel() {

        assertThat(line.getLogLevel(), is("INFO"));
    }

    @Test
    public void ctorParsesWarnLevel() {
        StringLogLine line = new StringLogLine("2009-06-07 13:23:57 demo.ZeroToFour main WARN: This is an informative message");

        assertThat(line.getLogLevel(), is("WARN"));
    }

    @Test
    public void ctorParsesDate() throws Exception {
        StringLogLine line = new StringLogLine("2009-06-07 13:23:57 demo.ZeroToFour main INFO: This is an informative message");


        SimpleDateFormat frm = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date expected = frm.parse("2009-06-07 13:23:57");
        assertThat(line.getDate(), is(expected));
    }
}
