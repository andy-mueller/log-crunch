package com.crudetech.sample.logcrunch;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;

public class LogFileTest {

    private TestLogFile testLogFile;

    @Before
    public void setUp() throws Exception {
        testLogFile = new TestLogFile("testLogFile");
    }


    @After
    public void after() throws Exception {
        testLogFile.delete();
    }

    @Test
    public void logLineIterableReturnsFileContent() throws Exception {
        LogFile logFile = new LogFile(testLogFile.getFile(), new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"), TestLogFile.Encoding);

        testLogFile.assertSameContent(logFile);
    }
}
