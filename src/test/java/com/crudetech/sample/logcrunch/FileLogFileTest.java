package com.crudetech.sample.logcrunch;


import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.text.SimpleDateFormat;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FileLogFileTest {

    private TestLogFile testLogFile;
    private LogFile fileLogFile;

    @Before
    public void setUp() throws Exception {
        testLogFile = new TestLogFile("testLogFile");
        BufferedReaderLogFile.LogLineFactory logLineFactory = new BufferedReaderLogFile.LogLineFactory() {
            @Override
            public StringLogLine newLogLine(String lineContent) {
                return new StringLogLine(lineContent, new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"));
            }
        };
        fileLogFile = new FileLogFile(testLogFile.getFile(), logLineFactory, TestLogFile.Encoding);

    }


    @After
    public void after() throws Exception {
        testLogFile.delete();
    }

    @Test
    public void logLineIterableReturnsFileContent() throws Exception {
        testLogFile.assertSameContent(fileLogFile);
    }
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Test
    public void closingLogFileInvalidatesLineIteration() {
        Iterator<StringLogLine> logLineIterator = fileLogFile.getLines().iterator();
        logLineIterator.next();
        assertThat(logLineIterator.hasNext(), is(true));

        fileLogFile.close();

        assertThat(logLineIterator.hasNext(), is(false));
        expectedException.expect(IllegalStateException.class);
        logLineIterator.next();
    }
}
