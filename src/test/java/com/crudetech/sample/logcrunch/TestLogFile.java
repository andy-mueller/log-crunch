package com.crudetech.sample.logcrunch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TestLogFile {
    private File file;
    static final String Line1 = "2009-06-07 13:23:57 demo.ZeroToFour main INFO: This is an informative message";
    static final String Line2 = "2009-06-07 13:25:57 demo.ZeroToFive subroutine WARN: This is another informative message";
    static final String Line3 = "2009-06-08 10:11:36 demo.ZeroToFive subroutine DEBUG: This is another informative message";
    static final Charset Encoding = Charset.forName("UTF-8");
    private final List<String> logLines;

    private static final TempDir tempDir = new TempDir();

    TestLogFile(String name) throws IOException {
        file = new File(tempDir, name);
        logLines = Collections.unmodifiableList(asList(
                Line1, Line2, Line3
        ));
        writeLinesToTestLogFile();
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

    void delete() throws IOException {
        if(!file.exists()){
            throw new RuntimeException("The test file does not exist. Cannot delete it!");
        }
        if (!file.delete()) {
            throw new IOException("Could not delete file. File is probably still open!");
        }
        if(file.exists()){
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
        Iterable<? extends StringLogLine> lines = logFile.getLines();
        ArrayList<String> actual = logLinesToString(lines);

        assertThat(actual, is(getLogLines()));

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
