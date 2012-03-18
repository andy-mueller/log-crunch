package com.crudetech.sample.logcrunch;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static com.crudetech.sample.logcrunch.LogFileMatcher.equalTo;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LogFileFilterInteractorTest {
    @Rule
    public InMemoryTestLogFile logFileStub1 = new InMemoryTestLogFile("machine101-20090610");
    @Rule
    public InMemoryTestLogFile logFileStub2 = new InMemoryTestLogFile("machine101-20090611");
    private LogFileLocator locator;
    private Interval searchDate;
    private LogFileNamePattern logFileNamePattern;

    @Before
    public void setUp() throws Exception {
        searchDate = new Interval(new DateTime(1000), new DateTime(1001));

        locator = mock(LogFileLocator.class);
        logFileNamePattern = new LogFileNamePattern("machine101-%d{yyyyMMdd}");
        when(locator.find(logFileNamePattern, searchDate)).thenReturn(asList((LogFile) logFileStub1, logFileStub2));
    }


    @Test
    public void levelFiltersAreApplied() {
        LogFileFilterInteractor interactor = new LogFileFilterInteractor(locator);

        LogFileFilterInteractor.RequestModel request = new LogFileFilterInteractor.RequestModel();
        request.logFileNamePattern = logFileNamePattern;
        request.dates.add(searchDate);
        request.levels.add(LogLevel.Error);

        Iterable<LogFile> logFiles = interactor.getFilteredLogFiles(request);

        LogFile expected1 = new StringReaderLogFile(new TestLogLineFactory(), logFileStub1.line4());
        LogFile expected2 = new StringReaderLogFile(new TestLogLineFactory(), logFileStub2.line4());

        assertLogFileIterablesEqual(logFiles, asList(expected1, expected2));
    }

    @Test
    public void findFiles() {
        LogFileFilterInteractor interactor = new LogFileFilterInteractor(locator);

        LogFileFilterInteractor.RequestModel request = new LogFileFilterInteractor.RequestModel();
        request.logFileNamePattern = logFileNamePattern;
        request.dates.add(searchDate);

        Iterable<LogFile> logFiles = interactor.getFilteredLogFiles(request);

        assertLogFileIterablesEqual(logFiles, asList((LogFile) logFileStub1, logFileStub2));
    }

    private void assertLogFileIterablesEqual(Iterable<LogFile> lhs, List<LogFile> rhs) {
        Iterator<LogFile> ilhs = lhs.iterator();
        Iterator<LogFile> irhs = rhs.iterator();

        while (ilhs.hasNext() && irhs.hasNext()) {
            assertThat(ilhs.next(), equalTo(irhs.next()));
        }

        assertThat(ilhs.hasNext(), is(irhs.hasNext()));
    }
}
