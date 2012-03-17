package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.MappingIterable;
import com.crudetech.sample.filter.UnaryFunction;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.rules.ExternalResource;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;


public abstract class TestLogFile extends ExternalResource implements LogFile{
    static final String Line1 = "2009-06-07 13:23:57 demo.ZeroToFour main INFO: This is an informative message";
    static final String Line2 = "2009-06-07 13:25:57 demo.ZeroToFive subroutine WARN: This is another informative message";
    static final String Line3 = "2009-06-08 10:11:36 demo.ZeroToFive subroutine DEBUG: This is another informative message";
    static final Charset Encoding = Charset.forName("UTF-8");
    static DateTimeFormatter DateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    protected List<String> logLines;
    protected final String name;
    private String line4;

    public TestLogFile(String name) {
        this.name = name;
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        line4 = generateUniqueLogline();
        logLines = Collections.unmodifiableList(asList(
                Line1, Line2, Line3, line4
        ));
    }

    private String generateUniqueLogline() {
        return MessageFormat.format("{0} {1} subroutine INFO: {2}", DateFormat.print(new DateTime()), getClass().getName(), UUID.randomUUID());
    }

    @Override
    public Iterable<LogLine> getLines() {
        return new MappingIterable<String, LogLine>(logLines, new UnaryFunction<LogLine, String>() {
            @Override
            public LogLine evaluate(String argument) {
                return new StringLogLine(argument, DateFormat);
            }
        });
    }

    @Override
    public void close() {
    }
}
