package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.Iterables;
import com.crudetech.sample.logcrunch.FileTestLogFile;
import org.joda.time.Interval;
import org.junit.Rule;
import org.junit.Test;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class LogCrunchFilterServletFileSystemTest {
    private static final Interval AllTimeInTheWorld = new Interval(0, Long.MAX_VALUE / 2);
    @Rule
    public FileTestLogFile logfile1 = new FileTestLogFile("machinename101-20070506.log");

    @Test
    public void foundFilesAreFilteredAndWrittenToOutput() throws Exception {
        LogCrunchFilterServlet servlet = new LogCrunchFilterServlet();
        ServletConfig configuration = new ServletConfigStub();
        servlet.init(configuration);

        HttpServletRequestStub request = new HttpServletRequestStub();
        request.putParameter(LogCrunchFilterServlet.RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(LogCrunchFilterServlet.RequestParameters.LogFileNamePattern, "machinename101-%d{yyyyMMdd}.log");


        HttpServletResponseStub response = new HttpServletResponseStub();

        servlet.doGet(request, response);


        List<String> expected = Iterables.copy(logfile1.getLinesAsString());
        assertThat(response.lines, is(expected));
    }

    static class HttpServletResponseStub extends HttpServletResponseWrapper {
        HttpServletResponseStub() {
            super(mock(HttpServletResponse.class));
        }

        final List<String> lines = new ArrayList<String>();
        private String tmp = "";

        @Override
        public PrintWriter getWriter() throws IOException {
            return new PrintWriter(mock(Writer.class)) {
                @Override
                public void print(String s) {
                    tmp += s;
                }

                @Override
                public void println() {
                    lines.add(tmp);
                    tmp = "";
                }
            };
        }
    }

    private class ServletConfigStub implements ServletConfig {
        @Override
        public String getServletName() {
            return "";
        }

        @Override
        public ServletContext getServletContext() {
            return null;
        }

        @Override
        public String getInitParameter(String name) {
            if (name.equals(LogCrunchFilterServlet.InitParameters.ConfigurationResource)) {
                return "logfilter-logcrunch-test.xml";
            }
            throw new IllegalArgumentException("Unknown parameter " + name);
        }

        @Override
        public Enumeration getInitParameterNames() {
            throw new UnsupportedOperationException();
        }
    }
}
