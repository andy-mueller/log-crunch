package com.crudetech.sample.logcrunch;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LogFileTest {

    private TestLogFile testLogFile;

    @Before
    public void setUp() throws Exception {
        testLogFile = new TestLogFile("testLogFile");
    }


    @After
    public void after() throws Exception {
        testLogFile.close();
    }

    @Test
    public void logLineIterableReturnsFileContent() throws Exception {
        LogFile logFile = new LogFile(testLogFile.getFile(), new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"), TestLogFile.Encoding);

        Iterable<? extends StringLogLine> lines = logFile.getLines();
        ArrayList<String> actual = logLinesToString(lines);

        assertThat(actual, is(testLogFile.getLogLines()));
    }

    private ArrayList<String> logLinesToString(Iterable<? extends StringLogLine> lines) {
        ArrayList<String> actual = new ArrayList<String>();
        for (StringLogLine s : lines) {
            StringWriter sw = new StringWriter();
            s.print(new PrintWriter(sw));
            actual.add(sw.toString());
        }
        return actual;
    }
}
