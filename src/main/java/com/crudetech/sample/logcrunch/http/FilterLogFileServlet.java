package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.FilterLogFileInteractor;
import com.crudetech.sample.logcrunch.InteractorFactory;
import com.crudetech.sample.logcrunch.LogFileNamePattern;
import com.crudetech.sample.logcrunch.LogLine;
import com.crudetech.sample.logcrunch.logback.LogbackLogFileNamePattern;
import org.joda.time.Interval;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;

public class FilterLogFileServlet extends HttpServlet {
    InteractorFactory<FilterLogFileInteractor> filterLogFileInteractorFactory;

    // GET http://localhost:8080/logcrunch/filter?logFileNamePattern=machinename101-%25d{yyyyMMdd}.log&searchRange=2007-05-06/2007-05-08&level=Info&level=Warn
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final HttpResponse response = new HttpResponse(resp);
        FilterLogFileInteractor.FilterQuery filterQuery = buildQuery(getParametersMap(req), response);
        if (response.isCommitted()) {
            return;
        }

        FilterLogFileInteractor filterLogFileInteractor = newInteractor();

        filterLogFileInteractor.getFilteredLines(filterQuery, new FilterLogFileInteractor.FilterResult() {
            @Override
            public void filteredLogLine(LogLine logLine) {
                response.writeLogLine(logLine);
            }

            @Override
            public void noFilesFound() {
                response.commitErrorResponse(HttpStatusCode.NotFound);
            }

            @Override
            public void noLinesFound() {
                response.commitResponse(HttpStatusCode.OkNoLinesFound);
            }
        });
    }

    private class HttpResponse {
        private final HttpServletResponse servletResponse;

        HttpResponse(HttpServletResponse servletResponse) {
            this.servletResponse = servletResponse;
        }

        void commitResponse(HttpStatusCode status) {
            getWriter().print(status.Message);
            servletResponse.setStatus(status.Code);
        }

        private PrintWriter getWriter() {
            try {
                return servletResponse.getWriter();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        void commitErrorResponse(HttpStatusCode code) {
            try {
                servletResponse.sendError(code.Code, code.Message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        void writeLogLine(LogLine logLine) {
            PrintWriter responseWriter = getWriter();
            logLine.print(responseWriter);
            responseWriter.println();
        }

        boolean isCommitted() {
            return servletResponse.isCommitted();
        }
    }

    private FilterLogFileInteractor.FilterQuery buildQuery(Map<String, String[]> parameterMap, HttpResponse resp) {
        FilterLogFileInteractor.FilterQuery filterQuery = new ParameterFilterQuery();
        ParameterMapper mapper = buildParameterMapper(parameterMap);
        try {
            mapper.mapTo(filterQuery);
        } catch (ParameterMapper.BadFormatException e) {
            resp.commitErrorResponse(HttpStatusCode.BadFormat);
        } catch (ParameterMapper.NoParameterException e) {
            resp.commitErrorResponse(HttpStatusCode.BadFormat);
        }
        return filterQuery;
    }


    private ParameterMapper buildParameterMapper(Map<String, String[]> parameterMap) {
        ParameterMapper mapper = new ParameterMapper(parameterMap);
        mapper.registerParameterFactory(Interval.class, new ParameterMapper.ReflectionParameterFactory("parse", String.class));
        mapper.registerParameterFactory(LogFileNamePattern.class, new ParameterMapper.ParameterFactory() {
            @Override
            public Object create(Class<?> parameterType, String parameterValue) {
                return new LogbackLogFileNamePattern(parameterValue);
            }
        });
        return mapper;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String[]> getParametersMap(HttpServletRequest req) {
        return req.getParameterMap();
    }

    FilterLogFileInteractor newInteractor() {
        return filterLogFileInteractorFactory.createInteractor();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        loadLodFileFilterFromConfig(config);
    }

    private void loadLodFileFilterFromConfig(ServletConfig config) {
        String configResource = config.getInitParameter(ServletInitParameters.ConfigurationResource);
        InputStream configFile = getClass().getResourceAsStream("/" + configResource);
        try {
            this.filterLogFileInteractorFactory = new XmlConfiguredInteractorFactory<FilterLogFileInteractor>(configFile);
        } finally {
            close(configFile);
        }
    }

    private void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
