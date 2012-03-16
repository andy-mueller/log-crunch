package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.FilterChain;
import com.crudetech.sample.filter.Predicate;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.crudetech.sample.Iterables.copy;
import static com.crudetech.sample.Iterables.getFirst;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LogFileFilterInteractorTest {

    private LogFile logFileStub;
    private LogFileLocator locator;
    private Date searchDate;
    private BufferedReaderLogFile.LogLineFactory loglineFactory;

    @Before
    public void setUp() throws Exception {
        searchDate = new Date(1000);
        loglineFactory = new TestLogLineFactory();
        String content = TestLogFile.Line1 + "\n" + TestLogFile.Line2;
        logFileStub = new StringLogFile(loglineFactory, content);

        locator = mock(LogFileLocator.class);
        LogFileNamePattern name = new LogFileNamePattern("machine101-%d{yyyyMMdd}");
        when(locator.find(name, new DateTimeRange(searchDate, new Date()))).thenReturn(asList(logFileStub));
    }

//    @Test
//    public void findFiles() {
//        FilterChain<LogLine> infoFilter = identityFilter();
//        LogFileFilterInteractor interactor = new LogFileFilterInteractor(locator);
//
//        Iterable<LogFile> logFiles = interactor.getFilteredLogFiles("machine101", searchDate);
//
//        assertThat(getFirst(logFiles), is(equalTo(logFileStub)));
//        assertThat(size(logFiles), is(1));
//    }

    @SuppressWarnings("unchecked")
    private FilterChain<LogLine> identityFilter() {
        FilterChain<LogLine> infoFilter = mock(FilterChain.class);
        when(infoFilter.apply(any(Iterable.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[0];
            }
        });
        return infoFilter;
    }

    private Matcher<LogFile> equalTo(final LogFile file) {
        return new TypeSafeMatcher<LogFile>() {
            @Override
            protected boolean matchesSafely(LogFile item) {
                List<LogLine> lines = copy(item.getLines());
                return lines.equals(copy(file.getLines()));
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(file);
            }
        };
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
//        List<LogLine> expected = asList(loglineFactory.newLogLine(TestLogFile.Line1));
//        assertThat(logLines, is(expected));
//    }

    @SuppressWarnings("unchecked")
    private Collection<Predicate<LogLine>> infoLevel() {
        Predicate<LogLine> isInfo = new Predicate<LogLine>() {
            @Override
            public Boolean evaluate(LogLine item) {
                return item.hasLogLevel(LogLevel.Info);
            }
        };
        return asList(isInfo);
    }

    @Ignore
    @Test
    public void filtersApplied() {
        LogFileFilterInteractor.RequestModel model = new LogFileFilterInteractor.RequestModel();
        model.logFileName = "machine101";
        model.dates.add(new DateTimeRange(searchDate, new Date()));
        model.levels.add(LogLevel.Info);

        LogFileFilterInteractor interactor = new LogFileFilterInteractor(locator, null);

        Iterable<LogFile> logFiles = interactor.getFilteredLogFiles(model);

        LogFile foundFile = getFirst(logFiles);
        List<LogLine> logLines = copy(foundFile.getLines());

        List<LogLine> expected = asList(loglineFactory.newLogLine(TestLogFile.Line1));
        assertThat(logLines, is(expected));
    }
}
