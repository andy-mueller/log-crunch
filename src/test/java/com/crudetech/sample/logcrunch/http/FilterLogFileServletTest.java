package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.filter.PredicateBuilder;
import com.crudetech.sample.filter.Predicates;
import com.crudetech.sample.logcrunch.*;
import com.crudetech.sample.logcrunch.logback.LogbackLogFileNamePattern;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FilterLogFileServletTest {

    private HttpServletRequestStub request;
    private HttpServletResponse response;
    private LogFileInteractorStub interactorStub;
    private FilterLogFileServlet filterLogFileServlet;
    private static final Interval AllTimeInTheWorld = new Interval(0, Long.MAX_VALUE / 2);
    private PrintWriterStub responseWriter;

    @Rule
    public InMemoryTestLogFile logfile = new InMemoryTestLogFile("foobo.log");

    static class PrintWriterStub extends PrintWriter {
        private String writtenObject;

        PrintWriterStub() {
            super(mock(OutputStream.class));
        }

        @Override
        public void print(String writtenObject) {
            this.writtenObject = writtenObject;
        }

        void assertWrittenObject(String expected){
            assertThat(writtenObject, is(notNullValue()));
            assertThat(writtenObject, is(expected));
        }
    }

    @Before
    public void setUp() throws Exception {
        request = new HttpServletRequestStub();
        response = mock(HttpServletResponse.class);
        responseWriter = new PrintWriterStub();
        when(response.getWriter()).thenReturn(responseWriter);
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
        request.putParameter(FilterLogFileServlet.RequestParameters.Level, "Info", "Debug");
        request.putParameter(FilterLogFileServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(FilterLogFileServlet.RequestParameters.LogFileNamePattern, "machine101-%d{yyyMMdd}.log");
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
        request.putParameter(FilterLogFileServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(FilterLogFileServlet.RequestParameters.LogFileNamePattern, "machine101-%d{yyyMMdd}.log");

        filterLogFileServlet.doGet(request, response);

        ParameterFilterQuery expectedQuery = new ParameterFilterQuery();
        expectedQuery.addSearchInterval(AllTimeInTheWorld);
        expectedQuery.setLogFileNamePattern(new LogbackLogFileNamePattern("machine101-%d{yyyMMdd}.log"));
        assertThat(expectedQuery, is(interactorStub.filterQuery));
    }

    @Test
    public void searchIntervalsAreExtractedToQuery() throws Exception {
        request.putParameter(FilterLogFileServlet.RequestParameters.SearchRange, "2007-05-07T13:55:22,100/2009-07-02");
        request.putParameter(FilterLogFileServlet.RequestParameters.LogFileNamePattern, "machine101-%d{yyyMMdd}.log");

        filterLogFileServlet.doGet(request, response);

        ParameterFilterQuery expectedQuery = new ParameterFilterQuery();
        Interval expectedInterval = new Interval(new DateTime(2007, 5, 7, 13, 55, 22, 100), new DateTime(2009, 7, 2, 0, 0));
        expectedQuery.addSearchInterval(expectedInterval);
        expectedQuery.setLogFileNamePattern(new LogbackLogFileNamePattern("machine101-%d{yyyMMdd}.log"));
        assertThat(expectedQuery, is(interactorStub.filterQuery));
    }

    @Test
    public void logFileNamePatternIsExtractedToQuery() throws Exception {
        request.putParameter(FilterLogFileServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(FilterLogFileServlet.RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");

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
        request.putParameter(FilterLogFileServlet.RequestParameters.SearchRange, "200705071355,100/2009-07-02");
        request.putParameter(FilterLogFileServlet.RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");

        filterLogFileServlet.doGet(request, response);

        verifyErrorResponse(response, FilterLogFileServlet.HttpStatusCode.BadFormat);
    }

    private void verifyErrorResponse(HttpServletResponse response, FilterLogFileServlet.HttpStatusCode code) throws IOException {
        verify(response).sendError(code.Code, code.Message);
    }

    @Test
    public void atLeastOneTimeIntervalIsRequired() throws Exception {
        request.putParameter(FilterLogFileServlet.RequestParameters.SearchRange);
        request.putParameter(FilterLogFileServlet.RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");

        filterLogFileServlet.doGet(request, response);

        verifyErrorResponse(response, FilterLogFileServlet.HttpStatusCode.BadFormat);
    }

    @Test
    public void logFileNameFormatIsIsRequired() throws Exception {
        request.putParameter(FilterLogFileServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(FilterLogFileServlet.RequestParameters.LogFileNamePattern);

        filterLogFileServlet.doGet(request, response);

        verifyErrorResponse(response, FilterLogFileServlet.HttpStatusCode.BadFormat);
    }

    @Test
    public void newInteractorUsesFactory() throws Exception {
        FilterLogFileServlet logFileServlet = new FilterLogFileServlet();
        logFileServlet.filterLogFileInteractorFactory = mock(FilterLogFileInteractorFactory.class);
        FilterLogFileInteractor logFileInteractor = mock(FilterLogFileInteractor.class);
        when(logFileServlet.filterLogFileInteractorFactory.createInteractor()).thenReturn(logFileInteractor);

        assertThat(logFileServlet.newInteractor(), is(logFileInteractor));
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

    static class FindAllFilterBuilderStub implements FilterLogFileInteractor.FilterBuilder{
        @Override
        public PredicateBuilder<LogLine> build(FilterLogFileInteractor.FilterQuery filterQuery, PredicateBuilder<LogLine> filterBuilder) {
            return filterBuilder.start(Predicates.isTrue());
        }
    }
    @Test
    public void logFilesAreClosedAfterRequest() throws Exception {
        FilterLogFileServlet logFileServlet = new FilterLogFileServlet();
        logFileServlet.filterLogFileInteractorFactory = mock(FilterLogFileInteractorFactory.class);
        CloseCountingLogFileLocatorStub locatorStub = new CloseCountingLogFileLocatorStub();
        FilterLogFileInteractor logFileInteractor = new FilterLogFileInteractor(locatorStub, asList(new FindAllFilterBuilderStub()));
        when(logFileServlet.filterLogFileInteractorFactory.createInteractor()).thenReturn(logFileInteractor);

        request.putParameter(FilterLogFileServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(FilterLogFileServlet.RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");
        request.putParameter(FilterLogFileServlet.RequestParameters.Level, "Info");

        logFileServlet.doGet(request, response);

        assertThat(locatorStub.closeCountingLogFile.closeCount, is(1));
    }

    @Test
    public void resultLogLinesAreWrittenToResponse() throws Exception {
        request.putParameter(FilterLogFileServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(FilterLogFileServlet.RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");

        interactorStub = new LogFileInteractorStub() {
            @Override
            public void getFilteredLines(FilterQuery filterQuery, FilterResult filterResult) {
                filterResult.filteredLogLine(TestLogFile.SampleInfoLine);
            }
        };


        filterLogFileServlet.doGet(request, response);

        PrintWriterStub expected = new PrintWriterStub();
        TestLogFile.SampleInfoLine.print(expected);
        responseWriter.assertWrittenObject(expected.writtenObject);
    }

    @Test
    public void givenNoFilesFound_NotFoundIsReturned() throws Exception {
        request.putParameter(FilterLogFileServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(FilterLogFileServlet.RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");

        interactorStub = new LogFileInteractorStub() {
            @Override
            public void getFilteredLines(FilterQuery filterQuery, FilterResult filterResult) {
                filterResult.noFilesFound();
            }
        };


        filterLogFileServlet.doGet(request, response);

        verifyErrorResponse(response, FilterLogFileServlet.HttpStatusCode.NotFound);
    }
    @Test
    public void givenNoLinesFound_OkAndMessageAreReturned() throws Exception {
        request.putParameter(FilterLogFileServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(FilterLogFileServlet.RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");

        interactorStub = new LogFileInteractorStub() {
            @Override
            public void getFilteredLines(FilterQuery filterQuery, FilterResult filterResult) {
                filterResult.noLinesFound();
            }
        };


        filterLogFileServlet.doGet(request, response);

        verify(response).setStatus(FilterLogFileServlet.HttpStatusCode.OkNoLinesFound.Code);
        assertThat(responseWriter.writtenObject, is(FilterLogFileServlet.HttpStatusCode.OkNoLinesFound.Message));
    }
}
