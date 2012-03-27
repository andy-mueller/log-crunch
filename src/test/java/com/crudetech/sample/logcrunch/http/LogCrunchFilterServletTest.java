package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.LogFileFilterInteractor;
import com.crudetech.sample.logcrunch.LogLevel;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LogCrunchFilterServletTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private LogFileFilterInteractor interactorStub;
    private LogCrunchFilterServlet logCrunchFilterServlet;
    private static final Interval AllTimeInTheWorld = new Interval(0, Long.MAX_VALUE/2);

    @Before
    public void setUp() throws Exception {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        interactorStub = mock(LogFileFilterInteractor.class);
        logCrunchFilterServlet = new LogCrunchFilterServlet() {
            @Override
            LogFileFilterInteractor newInteractor() {
                return interactorStub;
            }
        };
    }

    @Test
    public void logLevelsAreExtractedToQuery() throws Exception {
        when(request.getParameterValues(LogCrunchFilterServlet.RequestParameters.Level)).thenReturn(new String[]{"Info", "Debug"});
        when(request.getParameterValues(LogCrunchFilterServlet.RequestParameters.SearchRange)).thenReturn(new String[]{AllTimeInTheWorld.toString()});

        logCrunchFilterServlet.doGet(request, response);

        LogFileFilterInteractor.Query expectedQuery = new LogFileFilterInteractor.Query();
        expectedQuery.levels.add(LogLevel.Info);
        expectedQuery.levels.add(LogLevel.Debug);
        expectedQuery.searchIntervals.add(AllTimeInTheWorld);
        verify(interactorStub).getFilteredLogFiles(expectedQuery);
    }

    @Test
    public void noLogLevelsAllowed() throws Exception {
        when(request.getParameterValues(LogCrunchFilterServlet.RequestParameters.Level)).thenReturn(null);
        when(request.getParameterValues(LogCrunchFilterServlet.RequestParameters.SearchRange)).thenReturn(new String[]{AllTimeInTheWorld.toString()});

        logCrunchFilterServlet.doGet(request, response);

        LogFileFilterInteractor.Query expectedQuery = new LogFileFilterInteractor.Query();
        expectedQuery.searchIntervals.add(AllTimeInTheWorld);
        verify(interactorStub).getFilteredLogFiles(expectedQuery);
    }

    @Test
    public void searchIntervalsAreExtractedToQuery() throws Exception {
        when(request.getParameterValues(LogCrunchFilterServlet.RequestParameters.SearchRange))
                .thenReturn(new String[]{"2007-05-07T13:55:22,100/2009-07-02"});

        logCrunchFilterServlet.doGet(request, response);

        LogFileFilterInteractor.Query expectedQuery = new LogFileFilterInteractor.Query();
        Interval expectedInterval = new Interval(new DateTime(2007, 5, 7, 13, 55, 22, 100), new DateTime(2009, 7, 2, 0, 0));
        expectedQuery.searchIntervals.add(expectedInterval);
        verify(interactorStub).getFilteredLogFiles(expectedQuery);
    }

    @Test
    public void atLeastOneTimeIntervalIsRequired() throws Exception {
        when(request.getParameterValues(LogCrunchFilterServlet.RequestParameters.SearchRange))
                .thenReturn(null);


        logCrunchFilterServlet.doGet(request, response);

        verify(response).sendError(404, "There must be at least one search interval specified!");
    }
}
