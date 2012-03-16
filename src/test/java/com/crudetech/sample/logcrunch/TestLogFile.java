package com.crudetech.sample.logcrunch;

import org.junit.rules.ExternalResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TestLogFile extends ExternalResource {
    static final String Line1 = "2009-06-07 13:23:57 demo.ZeroToFour main INFO: This is an informative message";
    static final String Line2 = "2009-06-07 13:25:57 demo.ZeroToFive subroutine WARN: This is another informative message";
    static final String Line3 = "2009-06-08 10:11:36 demo.ZeroToFive subroutine DEBUG: This is another informative message";
    static final Charset Encoding = Charset.forName("UTF-8");
    static SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static final TempDir tempDir = new TempDir();

    private File file;
    private List<String> logLines;
    private final String name;
    private String line4;

    TestLogFile(String name){
        this.name = name;
     }

    private void writeLinesToTestLogFile() throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), Encoding));
        try {
            for (String logLine : logLines) {
                writer.println(logLine);
            }
        } finally {
            writer.close();
        }
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        line4 = MessageFormat.format("{0} {1} subroutine INFO: {2}", DateFormat.format(new Date()), getClass().getName(), UUID.randomUUID());
        file = new File(tempDir, name);
        logLines = Collections.unmodifiableList(asList(
                Line1, Line2, Line3, line4
        ));
        writeLinesToTestLogFile();

    }

    @Override
    protected void after() {
        super.after();
        if (!file.exists()) {
            throw new RuntimeException("The test file does not exist. Cannot delete it!");
        }
        if (!file.delete()) {
            throw new RuntimeException("Could not delete file. File is probably still open!");
        }
        if (file.exists()) {
            throw new RuntimeException("The test file does still not exist");
        }
    }

    File getFile() {
        return file;
    }

    List<String> getLogLines() {
        return logLines;
    }

    void assertSameContent(LogFile logFile) {
        Iterable<? extends LogLine> lines = logFile.getLines();
        ArrayList<String> actual = logLinesToString(lines);

        assertThat(actual, is(getLogLines()));

    }

    private ArrayList<String> logLinesToString(Iterable<? extends LogLine> lines) {
        ArrayList<String> actual = new ArrayList<String>();
        for (LogLine s : lines) {
            StringWriter sw = new StringWriter();
            s.print(new PrintWriter(sw));
            actual.add(sw.toString());
        }
        return actual;
    }
}
