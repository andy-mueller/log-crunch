package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.filter.Strings;
import com.crudetech.sample.logcrunch.FileTestLogFile;
import org.joda.time.Interval;
import org.junit.Rule;
import org.junit.Test;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

import static com.crudetech.sample.filter.Algorithm.accumulate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LogCrunchFullServletWithFileSystemTest {
    private static final Interval AllTimeInTheWorld = new Interval(0, Long.MAX_VALUE / 2);
    private static final String logFileName = "machinename101-20070506.log";
    @Rule
    public FileTestLogFile logfile1 = new FileTestLogFile(logFileName);

    @Test
    public void foundFilesAreFilteredAndWrittenToOutput() throws Exception {
        FilterLogFileServlet logFileServlet = new FilterLogFileServlet();
        ServletConfig configuration = new ServletConfigStub("logfilter-logcrunch-test.xml");
        logFileServlet.init(configuration);

        HttpServletRequestStub request = new HttpServletRequestStub();
        request.putParameter(RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(RequestParameters.LogFileNamePattern, "machinename101-%d{yyyyMMdd}.log");


        HttpServletResponseStub response = new HttpServletResponseStub();

        logFileServlet.doGet(request, response);

        CharSequence expected = concat(logfile1.getLinesAsString(), "\n");
        assertThat(response.content, is(expected));
    }

    private CharSequence concat(Iterable<? extends CharSequence> strings, CharSequence linebreak) {
        StringBuilder sb = accumulate(new StringBuilder(), strings, Strings.concat(linebreak));
        return sb.toString();
    }

    private class ServletConfigStub implements ServletConfig {
        private final String xmlConfig;

        private ServletConfigStub(String xmlConfig) {
            this.xmlConfig = xmlConfig;
        }

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
            if (name.equals(ServletInitParameters.ConfigurationResource)) {
                return xmlConfig;
            }
            throw new IllegalArgumentException("Unknown parameter " + name);
        }

        @Override
        public Enumeration getInitParameterNames() {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void foundFilesAreListedInOutput() throws Exception {
        ListLogFilesServlet logFileServlet = new ListLogFilesServlet();
        ServletConfig configuration = new ServletConfigStub("listLogFiles-logcrunch-test.xml");
        logFileServlet.init(configuration);

        HttpServletRequestStub request = new HttpServletRequestStub();
        request.putParameter(RequestParameters.SearchRange, AllTimeInTheWorld.toString());
        request.putParameter(RequestParameters.LogFileNamePattern, "machinename101-%d{yyyyMMdd}.log");


        HttpServletResponseStub response = new HttpServletResponseStub();

        logFileServlet.doGet(request, response);

        assertThat(response.content, is(logFileName+Strings.lineSeparator()));
    }

}
