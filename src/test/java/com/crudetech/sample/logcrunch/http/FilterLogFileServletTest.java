package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.filter.PredicateBuilder;
import com.crudetech.sample.filter.Predicates;
import com.crudetech.sample.filter.Strings;
import com.crudetech.sample.logcrunch.*;
import com.crudetech.sample.logcrunch.logback.LogbackLogFileNamePattern;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FilterLogFileServletTest {

    private HttpServletRequestStub request;
    private HttpServletResponseStub response;
    private LogFileInteractorStub interactorStub;
    private FilterLogFileServlet filterLogFileServlet;
    private static final Interval AllTimeInTheWorld = new Interval(0, Long.MAX_VALUE / 2);

    @Rule
    public InMemoryTestLogFile logfile = new InMemoryTestLogFile("foobo.log");

    @Before
    public void setUp() throws Exception {
        request = new HttpServletRequestStub();
        response = new HttpServletResponseStub();
        interactorStub = new LogFileInteractorStub();

        filterLogFileServlet = new FilterLogFileServlet() {
            @Override
            FilterLogFileInteractor newInteractor() {
                return getInteractorStub();
            }
        };
    }

    private FilterLogFileInteractor getInteractorStub() {
        return interactorStub;
    }

    static class LogFileInteractorStub extends FilterLogFileInteractor {
        private FilterQuery filterQuery;

        public LogFileInteractorStub() {
            super(mock(LogFileLocator.class), new ArrayList<FilterBuilder>());
        }

        @Override
        public void getFilteredLines(FilterQuery filterQuery, FilterResult filterResult) {
            this.filterQuery = filterQuery;
        }
    }

    @Test
    public void logLevelsAreExtractedToQuery() throws Exception {
        request.putParameter(RequestParameters.Level, "Info", "Debug");
        request.putParameter(RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(RequestParameters.LogFileNamePattern, "machine101-%d{yyyMMdd}.log");
        filterLogFileServlet.doGet(request, response);

        ParameterFilterQuery expectedQuery = new ParameterFilterQuery();
        expectedQuery.addLevel(LogLevel.Info);
        expectedQuery.addLevel(LogLevel.Debug);
        expectedQuery.addSearchInterval(AllTimeInTheWorld);
        expectedQuery.setLogFileNamePattern(new LogbackLogFileNamePattern("machine101-%d{yyyMMdd}.log"));

        assertThat(expectedQuery, is(interactorStub.filterQuery));
    }

    @Test
    public void noLogLevelsAllowed() throws Exception {
        request.putParameter(RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(RequestParameters.LogFileNamePattern, "machine101-%d{yyyMMdd}.log");

        filterLogFileServlet.doGet(request, response);

        ParameterFilterQuery expectedQuery = new ParameterFilterQuery();
        expectedQuery.addSearchInterval(AllTimeInTheWorld);
        expectedQuery.setLogFileNamePattern(new LogbackLogFileNamePattern("machine101-%d{yyyMMdd}.log"));
        assertThat(expectedQuery, is(interactorStub.filterQuery));
    }

    @Test
    public void searchIntervalsAreExtractedToQuery() throws Exception {
        request.putParameter(RequestParameters.SearchRange, "2007-05-07T13:55:22,100/2009-07-02");
        request.putParameter(RequestParameters.LogFileNamePattern, "machine101-%d{yyyMMdd}.log");

        filterLogFileServlet.doGet(request, response);

        ParameterFilterQuery expectedQuery = new ParameterFilterQuery();
        Interval expectedInterval = new Interval(new DateTime(2007, 5, 7, 13, 55, 22, 100), new DateTime(2009, 7, 2, 0, 0));
        expectedQuery.addSearchInterval(expectedInterval);
        expectedQuery.setLogFileNamePattern(new LogbackLogFileNamePattern("machine101-%d{yyyMMdd}.log"));
        assertThat(expectedQuery, is(interactorStub.filterQuery));
    }

    @Test
    public void logFileNamePatternIsExtractedToQuery() throws Exception {
        request.putParameter(RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");

        filterLogFileServlet.doGet(request, response);

        ParameterFilterQuery expectedQuery = new ParameterFilterQuery();
        expectedQuery.addSearchInterval(AllTimeInTheWorld);
        expectedQuery.setLogFileNamePattern(new LogbackLogFileNamePattern("xyz-%d{yyyMMdd}.log"));
        assertThat(expectedQuery, is(interactorStub.filterQuery));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void nonIsoDateTimeReturnsBadFormat() throws Exception {
        request.putParameter(RequestParameters.SearchRange, "200705071355,100/2009-07-02");
        request.putParameter(RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");

        filterLogFileServlet.doGet(request, response);

        verifyErrorResponse(response, HttpStatusCode.BadFormat);
    }

    private void verifyErrorResponse(HttpServletResponseStub response, HttpStatusCode code) throws IOException {
        assertThat(response.sc, is(code.Code));
        assertThat(response.content, is(code.Message));
    }

    @Test
    public void atLeastOneTimeIntervalIsRequired() throws Exception {
        request.putParameter(RequestParameters.SearchRange);
        request.putParameter(RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");

        filterLogFileServlet.doGet(request, response);

        verifyErrorResponse(response, HttpStatusCode.BadFormat);
    }

    @Test
    public void logFileNameFormatIsIsRequired() throws Exception {
        request.putParameter(RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(RequestParameters.LogFileNamePattern);

        filterLogFileServlet.doGet(request, response);

        verifyErrorResponse(response, HttpStatusCode.BadFormat);
    }

    @Test
    public void newInteractorUsesFactory() throws Exception {
        FilterLogFileServlet logFileServlet = new FilterLogFileServlet();
        logFileServlet.filterLogFileInteractorFactory = createInteractorFactoryMock();
        FilterLogFileInteractor logFileInteractor = mock(FilterLogFileInteractor.class);
        when(logFileServlet.filterLogFileInteractorFactory.createInteractor()).thenReturn(logFileInteractor);

        assertThat(logFileServlet.newInteractor(), is(logFileInteractor));
    }

    @SuppressWarnings("unchecked")
    private InteractorFactory<FilterLogFileInteractor> createInteractorFactoryMock() {
        return mock(InteractorFactory.class);
    }


    static class CloseCountingLogFileStub extends ArrayListLogFile {
        private int closeCount = 0;

        CloseCountingLogFileStub() {
            super(asList(TestLogFile.SampleInfoLine));
        }

        @Override
        public void close() {
            super.close();
            ++closeCount;
        }
    }

    static class CloseCountingLogFileLocatorStub implements LogFileLocator {
        final CloseCountingLogFileStub closeCountingLogFile = new CloseCountingLogFileStub();

        @Override
        public Iterable<LogFile> find(LogFileNamePattern fileName, Iterable<Interval> ranges) {
            return asList((LogFile) closeCountingLogFile);
        }
    }

    static class FindAllFilterBuilderStub implements FilterLogFileInteractor.FilterBuilder {
        @Override
        public PredicateBuilder<LogLine> build(FilterLogFileInteractor.FilterQuery filterQuery, PredicateBuilder<LogLine> filterBuilder) {
            return filterBuilder.start(Predicates.isTrue());
        }
    }

    @Test
    public void logFilesAreClosedAfterRequest() throws Exception {
        FilterLogFileServlet logFileServlet = new FilterLogFileServlet();
        logFileServlet.filterLogFileInteractorFactory = createInteractorFactoryMock();
        CloseCountingLogFileLocatorStub locatorStub = new CloseCountingLogFileLocatorStub();
        FilterLogFileInteractor logFileInteractor = new FilterLogFileInteractor(locatorStub, asList(new FindAllFilterBuilderStub()));
        when(logFileServlet.filterLogFileInteractorFactory.createInteractor()).thenReturn(logFileInteractor);

        request.putParameter(RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");
        request.putParameter(RequestParameters.Level, "Info");

        logFileServlet.doGet(request, response);

        assertThat(locatorStub.closeCountingLogFile.closeCount, is(1));
    }

    @Test
    public void resultLogLinesAreWrittenToResponse() throws Exception {
        request.putParameter(RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");

        interactorStub = new LogFileInteractorStub() {
            @Override
            public void getFilteredLines(FilterQuery filterQuery, FilterResult filterResult) {
                filterResult.filteredLogLine(TestLogFile.SampleInfoLine);
            }
        };


        filterLogFileServlet.doGet(request, response);


        assertThat(response.content, is(stringOf(TestLogFile.SampleInfoLine)));
    }
    private String stringOf(LogLine line){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        line.print(new PrintWriter(pw));
        pw.print(Strings.LineSeparator);
        return sw.toString();
    }

    @Test
    public void givenNoFilesFound_NotFoundIsReturned() throws Exception {
        request.putParameter(RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");

        interactorStub = new LogFileInteractorStub() {
            @Override
            public void getFilteredLines(FilterQuery filterQuery, FilterResult filterResult) {
                filterResult.noFilesFound();
            }
        };


        filterLogFileServlet.doGet(request, response);

        verifyErrorResponse(response, HttpStatusCode.NotFound);
    }

    @Test
    public void givenNoLinesFound_OkAndMessageAreReturned() throws Exception {
        request.putParameter(RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");

        interactorStub = new LogFileInteractorStub() {
            @Override
            public void getFilteredLines(FilterQuery filterQuery, FilterResult filterResult) {
                filterResult.noLinesFound();
            }
        };

        filterLogFileServlet.doGet(request, response);

        assertThat(response.sc, is(HttpStatusCode.OkNoLinesFound.Code));
        assertThat(response.content, is(HttpStatusCode.OkNoLinesFound.Message));
    }
}
