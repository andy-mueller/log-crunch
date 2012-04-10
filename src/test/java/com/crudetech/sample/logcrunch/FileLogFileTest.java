package com.crudetech.sample.logcrunch;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ConcurrentModificationException;
import java.util.Iterator;

import static com.crudetech.sample.logcrunch.LogFileMatcher.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FileLogFileTest {

    @Rule
    public FileTestLogFile fileTestLogFile = new FileTestLogFile("fileTestLogFile");
    private LogFile fileLogFile;

    @Before
    public void setUp() throws Exception {
        BufferedReaderLogFile.LogLineFactory logLineFactory = TestLogFile.logLineFactory();
        fileLogFile = new FileLogFile(fileTestLogFile.getFile(), logLineFactory, TestLogFile.Encoding);

    }

    @Test
    public void logLineIterableReturnsFileContent() throws Exception {
        assertThat(fileTestLogFile, is(equalTo(fileLogFile)));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void closingLogFileInvalidatesLineIteration() {
        Iterator<LogLine> logLineIterator = fileLogFile.getLines().iterator();
        logLineIterator.next();
        assertThat(logLineIterator.hasNext(), is(true));

        fileLogFile.close();

        assertThat(logLineIterator.hasNext(), is(false));
        expectedException.expect(ConcurrentModificationException.class);
        logLineIterator.next();
    }
}
