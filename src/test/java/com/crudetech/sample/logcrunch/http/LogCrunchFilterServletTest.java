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

public class LogCrunchFilterServletTest {

    private HttpServletRequestStub request;
    private HttpServletResponse response;
    private InteractorStub interactorStub;
    private LogCrunchFilterServlet logCrunchFilterServlet;
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
        interactorStub = new InteractorStub();

        logCrunchFilterServlet = new LogCrunchFilterServlet() {
            @Override
            LogFileFilterInteractor newInteractor() {
                return getInteractorStub();
            }
        };
    }

    private LogFileFilterInteractor getInteractorStub() {
        return interactorStub;
    }

    static class InteractorStub extends LogFileFilterInteractor {
        private FilterQuery filterQuery;

        public InteractorStub() {
            super(mock(LogFileLocator.class), new ArrayList<FilterBuilder>());
        }

        @Override
        public void getFilteredLines(FilterQuery filterQuery, FilterResult filterResult) {
            this.filterQuery = filterQuery;
        }
    }

    @Test
    public void logLevelsAreExtractedToQuery() throws Exception {
        request.putParameter(LogCrunchFilterServlet.RequestParameters.Level, "Info", "Debug");
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(LogCrunchFilterServlet.RequestParameters.LogFileNamePattern, "machine101-%d{yyyMMdd}.log");
        logCrunchFilterServlet.doGet(request, response);

        ParameterFilterQuery expectedQuery = new ParameterFilterQuery();
        expectedQuery.addLevel(LogLevel.Info);
        expectedQuery.addLevel(LogLevel.Debug);
        expectedQuery.addSearchInterval(AllTimeInTheWorld);
        expectedQuery.setLogFileNamePattern(new LogbackLogFileNamePattern("machine101-%d{yyyMMdd}.log"));

        assertThat(expectedQuery, is(interactorStub.filterQuery));
    }

    @Test
    public void noLogLevelsAllowed() throws Exception {
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(LogCrunchFilterServlet.RequestParameters.LogFileNamePattern, "machine101-%d{yyyMMdd}.log");

        logCrunchFilterServlet.doGet(request, response);

        ParameterFilterQuery expectedQuery = new ParameterFilterQuery();
        expectedQuery.addSearchInterval(AllTimeInTheWorld);
        expectedQuery.setLogFileNamePattern(new LogbackLogFileNamePattern("machine101-%d{yyyMMdd}.log"));
        assertThat(expectedQuery, is(interactorStub.filterQuery));
    }

    @Test
    public void searchIntervalsAreExtractedToQuery() throws Exception {
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange, "2007-05-07T13:55:22,100/2009-07-02");
        request.putParameter(LogCrunchFilterServlet.RequestParameters.LogFileNamePattern, "machine101-%d{yyyMMdd}.log");

        logCrunchFilterServlet.doGet(request, response);

        ParameterFilterQuery expectedQuery = new ParameterFilterQuery();
        Interval expectedInterval = new Interval(new DateTime(2007, 5, 7, 13, 55, 22, 100), new DateTime(2009, 7, 2, 0, 0));
        expectedQuery.addSearchInterval(expectedInterval);
        expectedQuery.setLogFileNamePattern(new LogbackLogFileNamePattern("machine101-%d{yyyMMdd}.log"));
        assertThat(expectedQuery, is(interactorStub.filterQuery));
    }

    @Test
    public void logFileNamePatternIsExtractedToQuery() throws Exception {
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(LogCrunchFilterServlet.RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");

        logCrunchFilterServlet.doGet(request, response);

        ParameterFilterQuery expectedQuery = new ParameterFilterQuery();
        expectedQuery.addSearchInterval(AllTimeInTheWorld);
        expectedQuery.setLogFileNamePattern(new LogbackLogFileNamePattern("xyz-%d{yyyMMdd}.log"));
        assertThat(expectedQuery, is(interactorStub.filterQuery));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void nonIsoDateTimeReturnsBadFormat() throws Exception {
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange, "200705071355,100/2009-07-02");
        request.putParameter(LogCrunchFilterServlet.RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");

        logCrunchFilterServlet.doGet(request, response);

        verifyErrorResponse(response, LogCrunchFilterServlet.HttpStatusCode.BadFormat);
    }

    private void verifyErrorResponse(HttpServletResponse response, LogCrunchFilterServlet.HttpStatusCode code) throws IOException {
        verify(response).sendError(code.Code, code.Message);
    }

    @Test
    public void atLeastOneTimeIntervalIsRequired() throws Exception {
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange);
        request.putParameter(LogCrunchFilterServlet.RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");

        logCrunchFilterServlet.doGet(request, response);

        verifyErrorResponse(response, LogCrunchFilterServlet.HttpStatusCode.BadFormat);
    }

    @Test
    public void logFileNameFormatIsIsRequired() throws Exception {
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(LogCrunchFilterServlet.RequestParameters.LogFileNamePattern);

        logCrunchFilterServlet.doGet(request, response);

        verifyErrorResponse(response, LogCrunchFilterServlet.HttpStatusCode.BadFormat);
    }

    @Test
    public void newInteractorUsesFactory() throws Exception {
        LogCrunchFilterServlet servlet = new LogCrunchFilterServlet();
        servlet.logFileFilterInteractorFactory = mock(LogFileFilterInteractorFactory.class);
        LogFileFilterInteractor interactor = mock(LogFileFilterInteractor.class);
        when(servlet.logFileFilterInteractorFactory.createInteractor()).thenReturn(interactor);

        assertThat(servlet.newInteractor(), is(interactor));
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

    static class FindAllFilterBuilderStub implements LogFileFilterInteractor.FilterBuilder{
        @Override
        public PredicateBuilder<LogLine> build(LogFileFilterInteractor.FilterQuery filterQuery, PredicateBuilder<LogLine> filterBuilder) {
            return filterBuilder.start(Predicates.isTrue());
        }
    }
    @Test
    public void logFilesAreClosedAfterRequest() throws Exception {
        LogCrunchFilterServlet servlet = new LogCrunchFilterServlet();
        servlet.logFileFilterInteractorFactory = mock(LogFileFilterInteractorFactory.class);
        CloseCountingLogFileLocatorStub locatorStub = new CloseCountingLogFileLocatorStub();
        LogFileFilterInteractor interactor = new LogFileFilterInteractor(locatorStub, asList(new FindAllFilterBuilderStub()));
        when(servlet.logFileFilterInteractorFactory.createInteractor()).thenReturn(interactor);

        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(LogCrunchFilterServlet.RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");
        request.putParameter(LogCrunchFilterServlet.RequestParameters.Level, "Info");

        servlet.doGet(request, response);

        assertThat(locatorStub.closeCountingLogFile.closeCount, is(1));
    }

    @Test
    public void resultLogLinesAreWrittenToResponse() throws Exception {
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(LogCrunchFilterServlet.RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");

        interactorStub = new InteractorStub() {
            @Override
            public void getFilteredLines(FilterQuery filterQuery, FilterResult filterResult) {
                filterResult.filteredLogLine(TestLogFile.SampleInfoLine);
            }
        };


        logCrunchFilterServlet.doGet(request, response);

        PrintWriterStub expected = new PrintWriterStub();
        TestLogFile.SampleInfoLine.print(expected);
        responseWriter.assertWrittenObject(expected.writtenObject);
    }

    @Test
    public void givenNoFilesFound_NotFoundIsReturned() throws Exception {
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(LogCrunchFilterServlet.RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");

        interactorStub = new InteractorStub() {
            @Override
            public void getFilteredLines(FilterQuery filterQuery, FilterResult filterResult) {
                filterResult.noFilesFound();
            }
        };


        logCrunchFilterServlet.doGet(request, response);

        verifyErrorResponse(response, LogCrunchFilterServlet.HttpStatusCode.NotFound);
    }
    @Test
    public void givenNoLinesFound_OkAndMessageAreReturned() throws Exception {
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(LogCrunchFilterServlet.RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");

        interactorStub = new InteractorStub() {
            @Override
            public void getFilteredLines(FilterQuery filterQuery, FilterResult filterResult) {
                filterResult.noLinesFound();
            }
        };


        logCrunchFilterServlet.doGet(request, response);

        verify(response).setStatus(LogCrunchFilterServlet.HttpStatusCode.OkNoLinesFound.Code);
        assertThat(responseWriter.writtenObject, is(LogCrunchFilterServlet.HttpStatusCode.OkNoLinesFound.Message));
    }
}
