package com.crudetech.sample.logcrunch;

import com.crudetech.sample.logcrunch.LogFileFilterInteractor.LogLevelFilterBuilder;
import com.crudetech.sample.logcrunch.LogFileFilterInteractor.SearchIntervalFilterBuilder;
import com.crudetech.sample.logcrunch.http.ParameterQuery;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
    private LogFileFilterInteractor interactor;

    @Before
    public void setUp() throws Exception {
        logFileNamePattern = mock(LogFileNamePattern.class);
        locator = mock(LogFileLocator.class);
        interactor = new LogFileFilterInteractor(
                locator,
                asList(new LogLevelFilterBuilder(), new SearchIntervalFilterBuilder()));
    }

    @Test
    public void findFiles() {
        Interval testInterval = new Interval(1, 5);
        setupLocator(asList(testInterval), logFileStub1, logFileStub2);
        setupLocator(logFileStub1, logFileStub2);

        ParameterQuery request = new ParameterQuery();
        request.setLogFileNamePattern(logFileNamePattern);
        request.addSearchInterval(testInterval);

        interactor.getFilteredLines(request, new CollectingLogLineReceiverStub());

        verify(locator).find(logFileNamePattern, asList(testInterval));
    }

    private void setupLocator(Iterable<Interval> intervals, LogFile... logFiles) {
        when(locator.find(logFileNamePattern, intervals)).thenReturn(asList(logFiles));
    }

    @SuppressWarnings("unchecked")
    private void setupLocator(LogFile... logFiles) {
        when(locator.find(eq(logFileNamePattern), any(Iterable.class))).thenReturn(asList(logFiles));
    }


    @Test
    public void levelFiltersAreApplied() {
        setupLocator(logFileStub1, logFileStub2);

        ParameterQuery request = new ParameterQuery();
        request.addSearchInterval(allTimeOfTheWorld());
        request.setLogFileNamePattern(logFileNamePattern);
        request.addLevel(LogLevel.Info);
        request.addLevel(LogLevel.Warn);

        CollectingLogLineReceiverStub lineCollector = new CollectingLogLineReceiverStub();
        interactor.getFilteredLines(request, lineCollector);


        List<LogLine> expectedLines = new ArrayList<LogLine>();
        expectedLines.addAll(asList(TestLogFile.SampleInfoLine, logFileStub1.logLines.get(2), logFileStub1.logLines.get(3)));
        expectedLines.addAll(asList(TestLogFile.SampleInfoLine, logFileStub2.logLines.get(2), logFileStub2.logLines.get(3)));

        assertThat(lineCollector.collectedLines, is(expectedLines));
    }

    private Interval allTimeOfTheWorld() {
        return new Interval(0, Long.MAX_VALUE);
    }


    static class CollectingLogLineReceiverStub implements LogFileFilterInteractor.FilteredLogLineReceiver {
        private final List<LogLine> collectedLines = new ArrayList<LogLine>();

        @Override
        public void receive(LogLine line) {
            collectedLines.add(line);
        }
    }

    @Test
    public void timeFiltersAreApplied() {
        setupLocator(logFileStub1);

        ParameterQuery request = new ParameterQuery();
        request.setLogFileNamePattern(logFileNamePattern);
        request.addSearchInterval(new Interval(TestLogFile.SampleInfoLineDate, TestLogFile.SampleInfoLineDate.plusSeconds(1)));


        CollectingLogLineReceiverStub lineCollector = new CollectingLogLineReceiverStub();
        interactor.getFilteredLines(request, lineCollector);

        assertThat(lineCollector.collectedLines, is(asList(TestLogFile.SampleInfoLine)));
    }
}
