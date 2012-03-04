package com.crudetech.sample.logcrunch;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LogFileTest {

    private File testLogFile;

    private List<String> logLines = Collections.unmodifiableList(asList(
            "2009-06-07 13:23:57 demo.ZeroToFour main INFO: This is an informative message",
            "2009-06-07 13:25:57 demo.ZeroToFive subroutine WARN: This is another informative message"
    ));
    private static final Charset UTF8 = Charset.forName("UTF-8");

    @Before
    public void setUp() throws Exception {
        testLogFile = File.createTempFile("testLogFile", null);
        writeLinesToTestLogFile();
    }

    private void writeLinesToTestLogFile() throws IOException {
        PrintWriter writer = new PrintWriter( new OutputStreamWriter(new FileOutputStream(testLogFile), UTF8));
        try {
            for (String logLine : logLines) {
                writer.println(logLine);
            }
        } finally {
            writer.close();
        }
    }

    @After
    public void after() {
        assertThat(testLogFile.delete(), is(true));
        assertThat(testLogFile.exists(), is(false));
    }

    @Test
    public void logLineIterableReturnsFileContent() throws Exception {
        LogFile logFile = new LogFile(testLogFile, new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"), UTF8);

        Iterable<? extends StringLogLine> lines = logFile.getLines();
        ArrayList<String> actual = logLinesToString(lines);

        assertThat(actual, is(logLines));
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
