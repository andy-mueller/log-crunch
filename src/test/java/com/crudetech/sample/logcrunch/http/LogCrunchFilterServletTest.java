package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.LogFileFilterInteractor;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LogCrunchFilterServletTest {
    @Test @Ignore
    public void logLevelsAreExtractedToQuery() throws Exception {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterValues("level")).thenReturn(new String[]{"Info", "Debug"});
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final LogFileFilterInteractor interactorStub = mock(LogFileFilterInteractor.class);

        final LogCrunchFilterServlet servlet = new LogCrunchFilterServlet(){
            @Override
            LogFileFilterInteractor newInteractor() {
                return interactorStub;
            }
        };
        servlet.doGet(request, response);

        LogFileFilterInteractor.Query expecetdQuery = new LogFileFilterInteractor.Query();
        verify(interactorStub).getFilteredLogFiles(expecetdQuery);
    }
}
