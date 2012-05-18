package com.crudetech.sample.logcrunch;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ListLogFilesInteractorTest {

    private List<LogFile> logFiles;
    private LogFileLocatorStub locator;
    private ListLogFilesInteractor finder;
    private ArrayListLogFile logFileStub;

    @Before
    public void setUp() throws Exception {
        logFileStub = new ArrayListLogFile();
        logFiles = Arrays.<LogFile>asList(logFileStub);
        locator = new LogFileLocatorStub(logFiles);
        finder = new ListLogFilesInteractor(locator);
    }

    @Test
    public void givenLocatorFindsFiles_fileListIsReturned(){
        ListLogFilesInteractor.Query query = new ListLogFilesInteractorQueryStub();
        ListLogFilesInteractorResultStub result= new ListLogFilesInteractorResultStub();

        finder.listFiles(query, result);

        assertThat(result.logFiles, is(logFiles));
    }


    private static class ListLogFilesInteractorResultStub implements ListLogFilesInteractor.Result {
        List<LogFile> logFiles = new ArrayList<LogFile>();

        @Override
        public void listFile(LogFile logFile) {
            logFiles.add(logFile);
        }
    }

    @Test
    public void fileNamePatternIsPassedToLocator(){
        LogFileNamePattern queryPattern = anyPattern();
        ListLogFilesInteractor.Query query = new ListLogFilesInteractorQueryStub(queryPattern, Collections.<Interval>emptyList());
        ListLogFilesInteractorResultStub result= new ListLogFilesInteractorResultStub();

        finder.listFiles(query, result);

        assertThat(locator.pattern, is(queryPattern));
    }

    private static LogFileNamePattern anyPattern() {
        return new LogFileNamePatternStub(false, DateTime.now());
    }

    private static class ListLogFilesInteractorQueryStub implements ListLogFilesInteractor.Query {
        private final LogFileNamePattern queryPattern;
        private final List<Interval> searchIntervals;

        private ListLogFilesInteractorQueryStub(LogFileNamePattern queryPattern, List<Interval> searchIntervals) {
            this.queryPattern = queryPattern;
            this.searchIntervals = searchIntervals;
        }

        public ListLogFilesInteractorQueryStub() {
            this(anyPattern(), asList(allTimeInTheWorld()));
        }

        @Override
        public LogFileNamePattern getLogFileNamePattern() {
            return queryPattern;
        }

        @Override
        public Iterable<Interval> getSearchIntervals() {
            return searchIntervals;
        }
    }

    private static Interval allTimeInTheWorld() {
        return new Interval(0, Long.MAX_VALUE/2);
    }

    @Test
    public void searchIntervalIsPassedToLocator(){
        LogFileNamePattern queryPattern = anyPattern();
        List<Interval> allTimeOfTheWorld = asList(allTimeInTheWorld());
        ListLogFilesInteractor.Query query = new ListLogFilesInteractorQueryStub(queryPattern, allTimeOfTheWorld);
        ListLogFilesInteractorResultStub result= new ListLogFilesInteractorResultStub();

        finder.listFiles(query, result);

        assertThat(locator.searchIntervals, is(allTimeOfTheWorld));
    }

    @Test
    public void filesAreClosed(){
        LogFileNamePattern queryPattern = anyPattern();
        List<Interval> allTimeOfTheWorld = asList(allTimeInTheWorld());
        ListLogFilesInteractor.Query query = new ListLogFilesInteractorQueryStub(queryPattern, allTimeOfTheWorld);
        ListLogFilesInteractorResultStub result= new ListLogFilesInteractorResultStub();

        finder.listFiles(query, result);


        assertThat(logFileStub.isClosed(), is(true));
    }
}
