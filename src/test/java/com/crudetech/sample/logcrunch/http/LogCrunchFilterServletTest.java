package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.InMemoryTestLogFile;
import com.crudetech.sample.logcrunch.LogFile;
import com.crudetech.sample.logcrunch.LogFileFilterInteractor;
import com.crudetech.sample.logcrunch.LogLevel;
import com.crudetech.sample.logcrunch.logback.LogbackLogFileNamePattern;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class LogCrunchFilterServletTest {

    private HttpServletRequestStub request;
    private HttpServletResponse response;
    private LogFileFilterInteractor interactorStub;
    private LogCrunchFilterServlet logCrunchFilterServlet;
    private static final Interval AllTimeInTheWorld = new Interval(0, Long.MAX_VALUE / 2);
    private PrintWriter responseWriter;

    @Rule
    public InMemoryTestLogFile logfile = new InMemoryTestLogFile("foobo.log");

    @Before
    public void setUp() throws Exception {
        request = new HttpServletRequestStub();
        response = mock(HttpServletResponse.class);
        responseWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(responseWriter);
        interactorStub = mock(LogFileFilterInteractor.class);
        when(interactorStub.getFilteredLogFiles(any(ParameterQuery.class))).thenReturn(Arrays.<LogFile>asList(logfile));
        logCrunchFilterServlet = new LogCrunchFilterServlet() {
            @Override
            LogFileFilterInteractor newInteractor() {
                return interactorStub;
            }
        };
    }

    @Test
    public void logLevelsAreExtractedToQuery() throws Exception {
        request.putParameter(LogCrunchFilterServlet.RequestParameters.Level, "Info", "Debug");
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(LogCrunchFilterServlet.RequestParameters.LogFileNamePattern, "machine101-%d{yyyMMdd}.log");
        logCrunchFilterServlet.doGet(request, response);

        ParameterQuery expectedQuery = new ParameterQuery();
        expectedQuery.addLevel(LogLevel.Info);
        expectedQuery.addLevel(LogLevel.Debug);
        expectedQuery.addSearchInterval(AllTimeInTheWorld);
        expectedQuery.setLogFileNamePattern(new LogbackLogFileNamePattern("machine101-%d{yyyMMdd}.log"));
        verify(interactorStub).getFilteredLogFiles(expectedQuery);
    }

    @Test
    public void noLogLevelsAllowed() throws Exception {
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(LogCrunchFilterServlet.RequestParameters.LogFileNamePattern, "machine101-%d{yyyMMdd}.log");

        logCrunchFilterServlet.doGet(request, response);

        ParameterQuery expectedQuery = new ParameterQuery();
        expectedQuery.addSearchInterval(AllTimeInTheWorld);
        expectedQuery.setLogFileNamePattern(new LogbackLogFileNamePattern("machine101-%d{yyyMMdd}.log"));
        verify(interactorStub).getFilteredLogFiles(expectedQuery);
    }

    @Test
    public void searchIntervalsAreExtractedToQuery() throws Exception {
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange, "2007-05-07T13:55:22,100/2009-07-02");
        request.putParameter(LogCrunchFilterServlet.RequestParameters.LogFileNamePattern, "machine101-%d{yyyMMdd}.log");

        logCrunchFilterServlet.doGet(request, response);

        ParameterQuery expectedQuery = new ParameterQuery();
        Interval expectedInterval = new Interval(new DateTime(2007, 5, 7, 13, 55, 22, 100), new DateTime(2009, 7, 2, 0, 0));
        expectedQuery.addSearchInterval(expectedInterval);
        expectedQuery.setLogFileNamePattern(new LogbackLogFileNamePattern("machine101-%d{yyyMMdd}.log"));
        verify(interactorStub).getFilteredLogFiles(expectedQuery);
    }
    @Test
    public void logFileNamePatternIsExtractedToQuery() throws Exception {
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(LogCrunchFilterServlet.RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");

        logCrunchFilterServlet.doGet(request, response);

        ParameterQuery expectedQuery = new ParameterQuery();
        expectedQuery.addSearchInterval(AllTimeInTheWorld);
        expectedQuery.setLogFileNamePattern(new LogbackLogFileNamePattern("xyz-%d{yyyMMdd}.log"));
        verify(interactorStub).getFilteredLogFiles(expectedQuery);
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
    public void filteredLogLinesAreWrittenToResponse() throws Exception {
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(LogCrunchFilterServlet.RequestParameters.LogFileNamePattern, "xyz-%d{yyyMMdd}.log");

        logCrunchFilterServlet.doGet(request, response);

        assertLogLinesAreInOutput();
    }

    private void assertLogLinesAreInOutput() {
        for(String line : logfile.getLinesAsString()){
            verify(responseWriter).print(line);
        }
        verify(responseWriter, times(4)).println();
    }

    @Test
    public void servletInitCreatesFilterInteractorFactory() throws Exception {
        ServletConfig config = mock(ServletConfig.class);
        when(config.getInitParameter(LogCrunchFilterServlet.InitParameters.SearchPath)).thenReturn("/some/path");
        when(config.getInitParameter(LogCrunchFilterServlet.InitParameters.Encoding)).thenReturn("UTF-8");
        // will be: %date{HH:mm:ss.SSS} %-4relative [%thread] %-5level %logger{35} - %msg %n
        when(config.getInitParameter(LogCrunchFilterServlet.InitParameters.LogLineFormat)).thenReturn("yyyyMMdd");
        logCrunchFilterServlet.init(config);

        assertThat(logCrunchFilterServlet.logFileFilterInteractorFactory, is(notNullValue()));
    }
    @Test
    public void newInteractorUsesFactory() throws Exception {
        LogCrunchFilterServlet servlet = new LogCrunchFilterServlet();
        servlet.logFileFilterInteractorFactory = mock(LogbackLogFileFilterInteractorFactory.class);
        LogFileFilterInteractor interactor = mock(LogFileFilterInteractor.class);
        when(servlet.logFileFilterInteractorFactory.createInteractor()).thenReturn(interactor);

        assertThat(servlet.newInteractor(), is(interactor));
    }
}
