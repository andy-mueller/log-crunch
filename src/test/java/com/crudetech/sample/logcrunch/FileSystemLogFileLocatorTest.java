package com.crudetech.sample.logcrunch;

import com.crudetech.sample.Iterables;
import org.joda.time.DateTime;
import org.joda.time.Interval;
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
        sixthOfMay2007 = new Interval(get6thOfMay(), getSeventhOfMay());
        noMatch = new Interval(firstDayOfTheWorld(), firstDayOfTheWorld().plusYears(1));
        namePattern = TestLogFile.LogbackBridge.createFileNamePattern("machinename101-%d{yyyyMMdd}");
    }

    private DateTime firstDayOfTheWorld() {
        return new DateTime(0);
    }

    private DateTime getSeventhOfMay() throws ParseException {
        return new DateTime(2007, 5, 7, 0, 0);
    }

    private DateTime get8thOfMay() throws ParseException {
        return new DateTime(2007, 5, 8, 0, 0);
    }

    private DateTime get6thOfMay() {
        return new DateTime(2007, 5, 6, 0, 0);
    }

    @Rule
    public FileTestLogFile fileTestLogFile20070506 = new FileTestLogFile("machinename101-20070506");

    @Rule
    public FileTestLogFile fileTestLogFile20070507 = new FileTestLogFile("machinename101-20070507");

    private FileSystemLogFileLocator newLocator() {
        return newLocator(FileTestLogFile.Directory);
    }

    private FileSystemLogFileLocator newLocator(File directory) {
        final Charset encoding = Charset.forName("UTF-8");
        final BufferedReaderLogFile.LogLineFactory logLineFactory = TestLogFile.logLineFactory();
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
        Interval sixthAndSeventhOfMay = new Interval(get6thOfMay(), get8thOfMay());
        Iterable<LogFile> located = locator.find(namePattern, asList(sixthOfMay2007, sixthAndSeventhOfMay));

        assertThat(Iterables.size(located), is(2));
    }

    @Test
    public void multipleFilesFoundWithMultipleIntervals() throws Exception {
        Interval seventhOfMay2007 = new Interval(getSeventhOfMay(), get8thOfMay());
        Iterable<LogFile> located = locator.find(namePattern, asList(sixthOfMay2007, seventhOfMay2007));

        assertThat(Iterables.size(located), is(2));
    }

    @Test
    public void multipleFilesFoundHaveCorrectContent() throws Exception {
        Interval seventhOfMay2007 = new Interval(getSeventhOfMay(), get8thOfMay());
        Iterable<LogFile> located = locator.find(namePattern, asList(sixthOfMay2007, seventhOfMay2007));

        List<LogFile> loc = copy(located);

        assertThat(loc.get(0), is(equalTo(fileTestLogFile20070506)));
        assertThat(loc.get(1), is(equalTo(fileTestLogFile20070507)));
    }


    @Test
    public void ctorThrowsWhenFileIsNoDirectory() {
        File noDirectory = new File("/tmp/dummy") {
            @Override
            public boolean isDirectory() {
                return false;
            }
        };

        expectedException.expect(IllegalArgumentException.class);
        newLocator(noDirectory);
    }
}
