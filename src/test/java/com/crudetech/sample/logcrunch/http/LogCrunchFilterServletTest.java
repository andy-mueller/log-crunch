package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.LogFileFilterInteractor;
import com.crudetech.sample.logcrunch.LogLevel;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LogCrunchFilterServletTest {

    private HttpServletRequestStub request;
    private HttpServletResponse response;
    private LogFileFilterInteractor interactorStub;
    private LogCrunchFilterServlet logCrunchFilterServlet;
    private static final Interval AllTimeInTheWorld = new Interval(0, Long.MAX_VALUE/2);

    @Before
    public void setUp() throws Exception {
        request = new HttpServletRequestStub();
        response = mock(HttpServletResponse.class);
        interactorStub = mock(LogFileFilterInteractor.class);
        logCrunchFilterServlet = new LogCrunchFilterServlet() {
            @Override
            LogFileFilterInteractor newInteractor() {
                return interactorStub;
            }
        };
    }

    static class HttpServletRequestStub extends HttpServletRequestWrapper {
        private Map<String, String[]> parameters = new HashMap<String, String[]>();
        HttpServletRequestStub() {
            super(mock(HttpServletRequest.class));
        }
        void putParameter(String name, String... value){
            parameters.put(name, value);
        }

        @Override
        public Map getParameterMap() {
            return parameters;
        }
    }

    @Test
    public void logLevelsAreExtractedToQuery() throws Exception {
        request.putParameter(LogCrunchFilterServlet.RequestParameters.Level, "Info", "Debug");
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());

        logCrunchFilterServlet.doGet(request, response);

        LogFileFilterInteractor.Query expectedQuery = new LogFileFilterInteractor.Query();
        expectedQuery.addLevel(LogLevel.Info);
        expectedQuery.addLevel(LogLevel.Debug);
        expectedQuery.addSearchInterval(AllTimeInTheWorld);
        verify(interactorStub).getFilteredLogFiles(expectedQuery);
    }

    @Test
    public void noLogLevelsAllowed() throws Exception {
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());

        logCrunchFilterServlet.doGet(request, response);

        LogFileFilterInteractor.Query expectedQuery = new LogFileFilterInteractor.Query();
        expectedQuery.addSearchInterval(AllTimeInTheWorld);
        verify(interactorStub).getFilteredLogFiles(expectedQuery);
    }

    @Test
    public void searchIntervalsAreExtractedToQuery() throws Exception {
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange, "2007-05-07T13:55:22,100/2009-07-02");

        logCrunchFilterServlet.doGet(request, response);

        LogFileFilterInteractor.Query expectedQuery = new LogFileFilterInteractor.Query();
        Interval expectedInterval = new Interval(new DateTime(2007, 5, 7, 13, 55, 22, 100), new DateTime(2009, 7, 2, 0, 0));
        expectedQuery.addSearchInterval(expectedInterval);
        verify(interactorStub).getFilteredLogFiles(expectedQuery);
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Test
    public void nonIsoDateTimeReturnsBadFormat() throws Exception {
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange, "200705071355,100/2009-07-02");

        logCrunchFilterServlet.doGet(request, response);

        verifyErrorResponse(response, LogCrunchFilterServlet.HttpStatusCode.BadFormat);
    }

    private void verifyErrorResponse(HttpServletResponse response, LogCrunchFilterServlet.HttpStatusCode code) throws IOException {
        verify(response).sendError(code.Code, code.Message);
    }

    @Test
    public void atLeastOneTimeIntervalIsRequired() throws Exception {
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange);

        logCrunchFilterServlet.doGet(request, response);

        verifyErrorResponse(response, LogCrunchFilterServlet.HttpStatusCode.BadFormat);
    }
}
