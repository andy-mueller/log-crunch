package com.crudetech.sample.logcrunch;

import com.crudetech.sample.Iterables;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.List;

import static com.crudetech.sample.Iterables.copy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class FileSystemLogFileLocatorTest {
    private LogFileLocator locator;
    private Interval sixthOfMay2007;
    private LogFileNamePattern namePattern;


    @Before
    public void setup() throws Exception {
        locator = newLocator();


        sixthOfMay2007 = new Interval(dateOf("20070506"), dateOf("20070507"));
        namePattern = new LogFileNamePattern("machinename101-%d{yyyyMMdd}");
    }

    private DateTime dateOf(String date) throws ParseException {
        DateTimeFormatter  dateFormat = DateTimeFormat.forPattern("yyyyMMdd");
        return dateFormat.parseDateTime(date);
    }

    @Rule
    public TestLogFile testLogFile20070506 = new TestLogFile("machinename101-20070506");
    @Rule
    public TestLogFile testLogFile20070507 = new TestLogFile("machinename101-20070507");

    private FileSystemLogFileLocator newLocator() {
        final Charset encoding = Charset.forName("UTF-8");
        final BufferedReaderLogFile.LogLineFactory logLineFactory = new TestLogLineFactory();
        FileSystemLogFileLocator.LogFileFactory logFileFactory = new FileSystemLogFileLocator.LogFileFactory() {
            @Override
            public LogFile create(File logFile) {
                return new FileLogFile(logFile, logLineFactory, encoding);
            }
        };
        return new FileSystemLogFileLocator(new TempDir(), logFileFactory);
    }

    @Test
    public void locationIsSuccessful() throws Exception {
        Iterable<LogFile> located = locator.find(namePattern, sixthOfMay2007);

        assertThat(Iterables.getFirst(located), is(notNullValue()));
        assertThat(Iterables.size(located), is(1));
    }

    @Test
    public void locatedFileHasCorrectContent() throws Exception {
        Iterable<LogFile> located = locator.find(namePattern, sixthOfMay2007);

        testLogFile20070506.assertSameContent(Iterables.getFirst(located));
    }

    @Test
    public void noLocationGivesNoLogFiles() throws Exception {
        Interval noMatch = new Interval(dateOf("20070101"), dateOf("20070201"));
        Iterable<LogFile> located = locator.find(namePattern, noMatch);

        assertThat(Iterables.size(located), is(0));
    }

    @Test
    public void multipleFilesFound() throws Exception {
        Interval noMatch = new Interval(dateOf("20070506"), dateOf("20070508"));
        Iterable<LogFile> located = locator.find(namePattern, noMatch);

        assertThat(Iterables.size(located), is(2));
    }

    @Test
    public void multipleFilesFoundHaveCorrectContent() throws Exception {
        Interval noMatch = new Interval(dateOf("20070506"), dateOf("20070508"));
        Iterable<LogFile> located = locator.find(namePattern, noMatch);

        List<LogFile> loc = copy(located);
        testLogFile20070506.assertSameContent(loc.get(0));
        testLogFile20070507.assertSameContent(loc.get(1));
    }
}
