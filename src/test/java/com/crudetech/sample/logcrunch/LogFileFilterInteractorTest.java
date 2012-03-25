package com.crudetech.sample.logcrunch;

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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LogFileFilterInteractorTest {
    @Rule
    public InMemoryTestLogFile logFileStub1 = new InMemoryTestLogFile("machine101-20090610");
    @Rule
    public InMemoryTestLogFile logFileStub2 = new InMemoryTestLogFile("machine101-20090611");
    private LogFileLocator locator;
    private LogFileNamePattern logFileNamePattern;

    @Before
    public void setUp() throws Exception {
        logFileNamePattern = new LogFileNamePattern("machine101-%d{yyyyMMdd}");
        locator = mock(LogFileLocator.class);
    }

    @Test
    public void findFiles() {
        Interval testInterval = new Interval(1, 5);
        when(locator.find(logFileNamePattern, asList(testInterval))).thenReturn(asList((LogFile) logFileStub1, logFileStub2));
        LogFileFilterInteractor interactor = new LogFileFilterInteractor(locator);

        LogFileFilterInteractor.Query request = new LogFileFilterInteractor.Query();
        request.logFileNamePattern = logFileNamePattern;
        request.searchIntervals.add(testInterval);

        interactor.getFilteredLogFiles(request);

        verify(locator).find(logFileNamePattern, asList(testInterval));
    }


    static class ArrayListLogFile implements LogFile {
        private final List<LogLine> lines;

        ArrayListLogFile(List<LogLine> lines) {
            this.lines = lines;
        }

        @Override
        public Iterable<LogLine> getLines() {
            return lines;
        }

        @Override
        public void close() {
        }
    }

    @Test
    public void levelFiltersAreApplied() {
        when(locator.find(eq(logFileNamePattern), any(List.class))).thenReturn(asList((LogFile) logFileStub1, logFileStub2));
        LogFileFilterInteractor interactor = new LogFileFilterInteractor(locator);

        LogFileFilterInteractor.Query request = new LogFileFilterInteractor.Query();
        request.searchIntervals.add(allTimeOfTheWorld());
        request.logFileNamePattern = logFileNamePattern;
        request.levels.add(LogLevel.Info);
        request.levels.add(LogLevel.Warn);

        Iterable<LogFile> logFiles = interactor.getFilteredLogFiles(request);

        LogFile expected1 = new ArrayListLogFile(asList(TestLogFile.SampleInfoLine, logFileStub1.logLines.get(2), logFileStub1.logLines.get(3)));
        LogFile expected2 = new ArrayListLogFile(asList(TestLogFile.SampleInfoLine, logFileStub2.logLines.get(2), logFileStub2.logLines.get(3)));

        assertLogFileIterablesEqual(logFiles, asList(expected1, expected2));
    }

    private Interval allTimeOfTheWorld() {
        return new Interval(0, Long.MAX_VALUE);
    }


    private void assertLogFileIterablesEqual(Iterable<LogFile> lhs, List<LogFile> rhs) {
        Iterator<LogFile> ilhs = lhs.iterator();
        Iterator<LogFile> irhs = rhs.iterator();

        int count = 0;
        while (ilhs.hasNext() && irhs.hasNext()) {
            assertThat(Integer.toString(count++), ilhs.next(), equalTo(irhs.next()));
        }

        assertThat("ranges have same length",ilhs.hasNext(), is(irhs.hasNext()));
    }

    @Test
    public void timeFiltersAreApplied() {
        when(locator.find(eq(logFileNamePattern), any(List.class))).thenReturn(asList((LogFile) logFileStub1));
        LogFileFilterInteractor interactor = new LogFileFilterInteractor(locator);

        LogFileFilterInteractor.Query request = new LogFileFilterInteractor.Query();
        request.logFileNamePattern = logFileNamePattern;
        request.searchIntervals.add(new Interval(TestLogFile.SampleInfoLineDate, TestLogFile.SampleInfoLineDate.plusSeconds(1)));


        Iterable<LogFile> logFiles = interactor.getFilteredLogFiles(request);


        LogFile expected = new ArrayListLogFile(asList(TestLogFile.SampleInfoLine));
        assertLogFileIterablesEqual(logFiles, asList(expected));
    }
}
