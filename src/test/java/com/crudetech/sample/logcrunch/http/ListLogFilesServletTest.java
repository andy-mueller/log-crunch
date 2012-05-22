package com.crudetech.sample.logcrunch.http;


import com.crudetech.sample.logcrunch.ListLogFilesInteractor;
import com.crudetech.sample.logcrunch.logback.LogbackLogFileNamePattern;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ListLogFilesServletTest {

    private ListLogFilesServlet servlet;
    private ListLogFilesInteractorStub listLogFilesInteractorStub;

    @Before
    public void setUp() throws Exception {
        listLogFilesInteractorStub = new ListLogFilesInteractorStub();
        servlet = new ListLogFilesServlet() {
            @Override
            ListLogFilesInteractor createInteractor() {
                return listLogFilesInteractorStub;
            }
        };
    }

    static class ListLogFilesInteractorStub extends ListLogFilesInteractor {
        private Query query;

        ListLogFilesInteractorStub() {
            super(null);
        }

        @Override
        public void listFiles(Query query, Result result) {
            this.query = query;
        }
    }

    @Test
    public void requestParametersAreMappedToQuery() throws Exception {
        HttpServletRequestStub request = new HttpServletRequestStub();
        request.putParameter("logFileNamePattern", "machinename101-%d{yyyyMMdd}.log");
        request.putParameter("searchRange", "2007-05-06/2007-05-08");

        servlet.doGet(request, new HttpServletResponseStub());

        ParameterListFilesQuery expectedQuery = new ParameterListFilesQuery();
        expectedQuery.setLogFileNamePattern(new LogbackLogFileNamePattern("machinename101-%d{yyyyMMdd}.log"));
        expectedQuery.addSearchInterval(Interval.parse("2007-05-06/2007-05-08"));

        assertThat(listLogFilesInteractorStub.query, is((ListLogFilesInteractor.Query)expectedQuery));
    }

    @Test
    public void givenInvalidParameters_servletReturnsBadFormat() throws Exception {
        HttpServletRequestStub request = new HttpServletRequestStub();
        request.putParameter("logFileNamePattern", "machinename101-%d{yyyyMMdd}.log");
        request.putParameter("searchRange", "This is invalid");

        HttpServletResponseStub response = new HttpServletResponseStub();
        servlet.doGet(request, response);

        assertResponseStatus(response, HttpStatusCode.BadFormat);
    }

    private void assertResponseStatus(HttpServletResponseStub response, HttpStatusCode code) {
        assertThat(response.sc, is(code.Code));
        assertThat(response.msg, is(code.Message));
    }

    @Test
    public void givenMissingParameters_servletReturnsBadFormat() throws Exception {
        HttpServletRequestStub request = new HttpServletRequestStub();
        request.putParameter("logFileNamePattern", "machinename101-%d{yyyyMMdd}.log");
        request.putParameter("searchRange");

        HttpServletResponseStub response = new HttpServletResponseStub();
        servlet.doGet(request, response);

        assertResponseStatus(response, HttpStatusCode.BadFormat);
    }
    // found->result
    //not found->404
}
