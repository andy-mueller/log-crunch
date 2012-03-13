package com.crudetech.sample.logcrunch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class FileSystemLogFileLocatorTest {
    private TestLogFile testLogFile1;
    private LogFileLocator locator;
    private Date sixthOfMay2007;

    @Before
    public void addTestLogFiles() throws Exception {
        testLogFile1 = new TestLogFile("machinename101-20070506");
        locator = newLocator();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        sixthOfMay2007 = dateFormat.parse("20070506");
    }
    @Before
    public void setup() throws Exception {
        locator = newLocator();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        sixthOfMay2007 = dateFormat.parse("20070506");
    }


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

    @After
    public void after() throws IOException {
        testLogFile1.delete();
    }


    @Test
    public void locationIsSuccessful() throws Exception {
        LogFile located = locator.find("machinename101", sixthOfMay2007);

        assertThat(located, is(notNullValue())) ;
    }
    @Test
    public void locatedFileHasCorrectContent() throws Exception {
        LogFile located = locator.find("machinename101", sixthOfMay2007);

        testLogFile1.assertSameContent(located);
    }
}
