package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.LogFileFilterInteractor;
import com.crudetech.sample.logcrunch.LogLevel;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LogCrunchFilterServletTest {
    @Test
    public void logLevelsAreExtractedToQuery() throws Exception {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterValues(LogCrunchFilterServlet.RequestParameters.Level)).thenReturn(new String[]{"Info", "Debug"});
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final LogFileFilterInteractor interactorStub = mock(LogFileFilterInteractor.class);

        final LogCrunchFilterServlet servlet = new LogCrunchFilterServlet(){
            @Override
            LogFileFilterInteractor newInteractor() {
                return interactorStub;
            }
        };
        servlet.doGet(request, response);

        LogFileFilterInteractor.Query expectedQuery = new LogFileFilterInteractor.Query();
        expectedQuery.levels.add(LogLevel.Info);
        expectedQuery.levels.add(LogLevel.Debug);
        verify(interactorStub).getFilteredLogFiles(expectedQuery);
    }

    @Test
    public void noLogLevelsAllowed() throws Exception {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterValues(LogCrunchFilterServlet.RequestParameters.Level)).thenReturn(null);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final LogFileFilterInteractor interactorStub = mock(LogFileFilterInteractor.class);

        final LogCrunchFilterServlet servlet = new LogCrunchFilterServlet(){
            @Override
            LogFileFilterInteractor newInteractor() {
                return interactorStub;
            }
        };
        servlet.doGet(request, response);

        LogFileFilterInteractor.Query expectedQuery = new LogFileFilterInteractor.Query();
        verify(interactorStub).getFilteredLogFiles(expectedQuery);
    }
}
