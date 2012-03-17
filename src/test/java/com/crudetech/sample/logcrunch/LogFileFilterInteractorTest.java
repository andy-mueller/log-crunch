package com.crudetech.sample.logcrunch;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;

import static com.crudetech.sample.Iterables.getFirst;
import static com.crudetech.sample.Iterables.size;
import static com.crudetech.sample.logcrunch.LogFileMatcher.equalTo;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LogFileFilterInteractorTest {

    private LogFile logFileStub1;
    private LogFileLocator locator;
    private Interval searchDate;
    private BufferedReaderLogFile.LogLineFactory loglineFactory;
    private LogFileNamePattern logFileNamePattern;

    @Before
    public void setUp() throws Exception {
        searchDate = new Interval(new DateTime(1000), new DateTime(1001));
        loglineFactory = new TestLogLineFactory();
        String content = TestLogFile.Line1 + "\n" + TestLogFile.Line2;
        logFileStub1 = new StringLogFile(loglineFactory, content);
//        logFileStub2 = new StringLogFile(loglineFactory, content);

        locator = mock(LogFileLocator.class);
        logFileNamePattern = new LogFileNamePattern("machine101-%d{yyyyMMdd}");
        when(locator.find(logFileNamePattern, searchDate)).thenReturn(asList(logFileStub1));
    }




    static class StringLogFile extends BufferedReaderLogFile {
        private final String content;

        StringLogFile(LogLineFactory logLineFactory, String content) {
            super(logLineFactory);
            this.content = content;
        }

        @Override
        protected Reader createNewReader() {
            return new StringReader(content);
        }
    }


    @Ignore
    @Test
    public void filtersApplied() {
//        LogFileFilterInteractor.RequestModel model = new LogFileFilterInteractor.RequestModel();
//        model.logFileNamePattern = "machine101";
//        model.dates.add(new Interval(searchDate, new DateTime()));
//        model.levels.add(LogLevel.Info);
//
//        LogFileFilterInteractor interactor = new LogFileFilterInteractor(locator, null);
//
//        Iterable<LogFile> logFiles = interactor.getFilteredLogFiles(model);
//
//        LogFile foundFile = getFirst(logFiles);
//        List<LogLine> logLines = copy(foundFile.getLines());
//
//        List<LogLine> expected = asList(loglineFactory.newLogLine(FileTestLogFile.Line1));
//        assertThat(logLines, is(expected));
    }

    @Test
    public void findFiles() {
        LogFileFilterInteractor interactor = new LogFileFilterInteractor(locator);

        LogFileFilterInteractor.RequestModel request = new LogFileFilterInteractor.RequestModel();
        request.logFileNamePattern = logFileNamePattern;
        request.dates.add(searchDate);

        Iterable<LogFile> logFiles = interactor.getFilteredLogFiles(request);

        assertThat(getFirst(logFiles), is(equalTo(logFileStub1)));
        assertThat(size(logFiles), is(1));
    }

//    @Test
//    public void foundFilesAreFiltered() {
//        FilterChain<LogLine> infoFilter = new FilterChain<LogLine>(infoLevel());
//        LogFileFilterInteractor interactor = new LogFileFilterInteractor(locator);
//
//        Iterable<LogFile> logFiles = interactor.getFilteredLogFiles("machine101", searchDate);
//
//        LogFile foundFile = getFirst(logFiles);
//        List<LogLine> logLines = copy(foundFile.getLines());
//
//        List<LogLine> expected = asList(loglineFactory.newLogLine(FileTestLogFile.Line1));
//        assertThat(logLines, is(expected));
//    }

}
