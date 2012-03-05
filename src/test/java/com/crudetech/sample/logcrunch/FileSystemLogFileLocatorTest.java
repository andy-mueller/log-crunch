package com.crudetech.sample.logcrunch;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class FileSystemLogFileLocatorTest {
    private static File logFilePath = null;
    private static File dummyFile = null;
    private TestLogFile testLogFile1;

    @BeforeClass
    public static void beforeClass() throws IOException {
        dummyFile = File.createTempFile("dummy", null);
        logFilePath = dummyFile.getParentFile();
    }
    @AfterClass
    public static void afterClass(){
        dummyFile.delete();
    }

    @Before
    public void addTestLogFiles() throws IOException {
        testLogFile1 = new TestLogFile("machinename101-20070506");
    }
    @After
    public void after() throws IOException {
        testLogFile1.delete();
    }
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    @Test
    public void ctor(){
        FileSystemLogFileLocator locator = new FileSystemLogFileLocator(logFilePath);
    }

    @Test
    public void locationIsSuccessful() throws Exception {
        FileSystemLogFileLocator locator = new FileSystemLogFileLocator(logFilePath);

        Date sixthOfJune = dateFormat.parse("20070506");
        LogFile located = locator.find("machinename101", sixthOfJune);

        assertThat(located, is(notNullValue())) ;
    }
    @Test
    public void locatedFileHasCorrectContent() throws Exception {
        FileSystemLogFileLocator locator = new FileSystemLogFileLocator(logFilePath);

        Date sixthOfMay2007 = dateFormat.parse("20070506");
        LogFile located = locator.find("machinename101", sixthOfMay2007);

        testLogFile1.assertSameContent(located);
    }
}
