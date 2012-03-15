package com.crudetech.sample.logcrunch;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class FileSystemLogFileLocatorTest {
    private LogFileLocator locator;
    private DateTimeRange sixthOfMay2007;


    @Before
    public void setup() throws Exception {
        locator = newLocator();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        sixthOfMay2007 = new DateTimeRange(dateFormat.parse("20070506"), dateFormat.parse("20070507"));
    }

    @Rule
    public TestLogFile testLogFile = new TestLogFile("machinename101-20070506");

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
        LogFile located = locator.find("machinename101", sixthOfMay2007);

        assertThat(located, is(notNullValue()));
    }

    @Test
    public void locatedFileHasCorrectContent() throws Exception {
        LogFile located = locator.find("machinename101", sixthOfMay2007);

        testLogFile.assertSameContent(located);
    }
}
