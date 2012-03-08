package com.crudetech.sample.logcrunch;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;

public class FileLogFileTest {

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
        BufferedReaderLogFile.LogLineFactory logLineFactory = new BufferedReaderLogFile.LogLineFactory() {
            @Override
            public StringLogLine newLogLine(String lineContent) {
                return new StringLogLine(lineContent, new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"));
            }
        };
        BufferedReaderLogFile logFile = new FileLogFile(testLogFile.getFile(), logLineFactory, TestLogFile.Encoding);

        testLogFile.assertSameContent(logFile);
    }
}
