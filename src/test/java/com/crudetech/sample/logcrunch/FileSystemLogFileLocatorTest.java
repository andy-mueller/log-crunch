package com.crudetech.sample.logcrunch;

import com.crudetech.sample.Iterables;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.List;

import static com.crudetech.sample.Iterables.copy;
import static com.crudetech.sample.logcrunch.LogFileMatcher.equalTo;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class FileSystemLogFileLocatorTest {
    private LogFileLocator locator;
    private Interval sixthOfMay2007;
    private LogFileNamePattern namePattern;
    private Interval noMatch;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Before
    public void setup() throws Exception {
        locator = newLocator();


        sixthOfMay2007 = new Interval(dateOf("20070506"), dateOf("20070507"));
        namePattern = new LogFileNamePattern("machinename101-%d{yyyyMMdd}");
        noMatch = new Interval(dateOf("19720815"), dateOf("19730815"));
    }

    private DateTime dateOf(String date) throws ParseException {
        DateTimeFormatter  dateFormat = DateTimeFormat.forPattern("yyyyMMdd");
        return dateFormat.parseDateTime(date);
    }

    @Rule
    public FileTestLogFile fileTestLogFile20070506 = new FileTestLogFile("machinename101-20070506");
    @Rule
    public FileTestLogFile fileTestLogFile20070507 = new FileTestLogFile("machinename101-20070507");

    private FileSystemLogFileLocator newLocator() {
        return newLocator(new TempDir());
    }
    private FileSystemLogFileLocator newLocator(File directory) {
        final Charset encoding = Charset.forName("UTF-8");
        final BufferedReaderLogFile.LogLineFactory logLineFactory = new TestLogLineFactory();
        FileSystemLogFileLocator.LogFileFactory logFileFactory = new FileSystemLogFileLocator.LogFileFactory() {
            @Override
            public LogFile create(File logFile) {
                return new FileLogFile(logFile, logLineFactory, encoding);
            }
        };
        return new FileSystemLogFileLocator(directory, logFileFactory);
    }

    @Test
    public void locationIsSuccessful() throws Exception {
        Iterable<LogFile> located = locator.find(namePattern, asList(sixthOfMay2007));

        assertThat(Iterables.getFirst(located), is(notNullValue()));
        assertThat(Iterables.size(located), is(1));
    }

    @Test
    public void locatedFileHasCorrectContent() throws Exception {
        Iterable<LogFile> located = locator.find(namePattern, asList(sixthOfMay2007));

        assertThat(Iterables.getFirst(located), is(equalTo(fileTestLogFile20070506)));
    }

    @Test
    public void noLocationGivesNoLogFiles() throws Exception {
        Iterable<LogFile> located = locator.find(namePattern, asList(noMatch));
        assertThat(Iterables.size(located), is(0));
    }

    @Test
    public void multipleFilesFound() throws Exception {
        Interval seventhOfMay2007 = new Interval(dateOf("20070507"), dateOf("20070508"));
        Iterable<LogFile> located = locator.find(namePattern, asList(sixthOfMay2007, seventhOfMay2007));

        assertThat(Iterables.size(located), is(2));
    }

    @Test
    public void multipleFilesFoundHaveCorrectContent() throws Exception {
        Interval seventhOfMay2007 = new Interval(dateOf("20070507"), dateOf("20070508"));
        Iterable<LogFile> located = locator.find(namePattern, asList(sixthOfMay2007, seventhOfMay2007));

        List<LogFile> loc = copy(located);

        assertThat(loc.get(0), is(equalTo(fileTestLogFile20070506)));
        assertThat(loc.get(1), is(equalTo(fileTestLogFile20070507)));
    }
    @Test
    public void ctorThrowsWhenFileOsNoDirectory(){
        File noDirectory = new File("/tmp/dummy"){
            @Override
            public boolean isDirectory() {
                return false;
            }
        };

        expectedException.expect(IllegalArgumentException.class);
        newLocator(noDirectory);
    }
}
