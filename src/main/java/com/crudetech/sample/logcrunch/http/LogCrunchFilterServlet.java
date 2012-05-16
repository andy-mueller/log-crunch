package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.FilterLogFileInteractor;
import com.crudetech.sample.logcrunch.FilterLogFileInteractorFactory;
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

public class LogCrunchFilterServlet extends HttpServlet {
    FilterLogFileInteractorFactory filterLogFileInteractorFactory;

    class RequestParameters {
        static final String Level = "level";
        static final String SearchRange = "searchRange";
        static final String LogFileNamePattern = "logFileNamePattern";
    }

    static enum HttpStatusCode {
        Ok(200, ""),
        OkNoLinesFound(200, "No lines found that match the criteria"),
        BadFormat(400, "Bad format!"),
        NotFound(404, "No files found!");
        final String Message;
        final int Code;

        HttpStatusCode(int code, String message) {
            this.Code = code;
            Message = message;
        }
    }

    static class InitParameters {
        static final String ConfigurationResource = "configurationResource";
    }


    // GET http://localhost:8080/logcrunch/filter?logFileNamePattern=machinename101-%25d{yyyyMMdd}.log&searchRange=2007-05-06/2007-05-08&level=Info&level=Warn
    @Override
    protected void doGet(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        FilterLogFileInteractor.FilterQuery filterQuery = new ParameterFilterQuery();

        ParameterMapper mapper = buildParameterMapper(req);

        try {
            mapper.mapTo(filterQuery);
        } catch (ParameterMapper.BadFormatException e) {
            sendErrorResponse(resp, HttpStatusCode.BadFormat);
            return;
        } catch (ParameterMapper.NoParameterException e) {
            sendErrorResponse(resp, HttpStatusCode.BadFormat);
            return;
        }


        FilterLogFileInteractor filterLogFileInteractor = newInteractor();

        final PrintWriter responseWriter = resp.getWriter();
        filterLogFileInteractor.getFilteredLines(filterQuery, new FilterLogFileInteractor.FilterResult() {
            @Override
            public void filteredLogLine(LogLine logLine) {
                logLine.print(responseWriter);
                responseWriter.println();
            }

            @Override
            public void noFilesFound() {
                sendErrorResponse(resp, HttpStatusCode.NotFound);
            }

            @Override
            public void noLinesFound() {
                responseWriter.print(HttpStatusCode.OkNoLinesFound.Message);
                resp.setStatus(HttpStatusCode.OkNoLinesFound.Code);
            }
        });
    }

    private void sendErrorResponse(HttpServletResponse resp, HttpStatusCode code) {
        try {
            resp.sendError(code.Code, code.Message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ParameterMapper buildParameterMapper(HttpServletRequest req) {
        ParameterMapper mapper = new ParameterMapper(getParametersMap(req));
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
        String configResource = config.getInitParameter(InitParameters.ConfigurationResource);
        InputStream configFile = getClass().getResourceAsStream("/" + configResource);
        try {
            this.filterLogFileInteractorFactory = new XmlConfiguredFilterLogFileInteractorFactory(configFile);
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
