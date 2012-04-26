package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.*;
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
    LogFileFilterInteractorFactory logFileFilterInteractorFactory;

    class RequestParameters {
        static final String Level = "level";
        static final String SearchRange = "searchRange";
        static final String LogFileNamePattern = "logFileNamePattern";
    }

    static enum HttpStatusCode {
        Ok(200, ""), BadFormat(400, "Bad format!");
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LogFileFilterInteractor.Query query = new ParameterQuery();

        ParameterMapper mapper = new ParameterMapper(getParametersMap(req));
        mapper.registerParameterFactory(Interval.class, new ParameterMapper.ReflectionParameterFactory("parse", String.class));
        mapper.registerParameterFactory(LogFileNamePattern.class, new ParameterMapper.ParameterFactory() {
            @Override
            public Object create(Class<?> parameterType, String parameterValue) {
                return new LogbackLogFileNamePattern(parameterValue);
            }
        });

        try {
            mapper.mapTo(query);
        } catch (ParameterMapper.BadFormatException e) {
            resp.sendError(HttpStatusCode.BadFormat.Code, HttpStatusCode.BadFormat.Message);
            return;
        } catch (ParameterMapper.NoParameterException e) {
            resp.sendError(HttpStatusCode.BadFormat.Code, HttpStatusCode.BadFormat.Message);
            return;
        }


        LogFileFilterInteractor logFileFilterInteractor = newInteractor();
        Iterable<LogFile> filteredLogFiles = logFileFilterInteractor.getFilteredLogFiles(query);
        writeFilteredLogFilesToOutputStream(resp.getWriter(), filteredLogFiles);
        close(filteredLogFiles);
    }

    private void close(Iterable<LogFile> logFiles) {
        for (LogFile logFile : logFiles) {
            logFile.close();
        }
    }

    private void writeFilteredLogFilesToOutputStream(PrintWriter responseWriter, Iterable<LogFile> filteredLogFiles) throws IOException {
        for (LogFile filteredLogFile : filteredLogFiles) {
            writeFilteredLogLineToOutputStream(responseWriter, filteredLogFile);
        }
    }

    private void writeFilteredLogLineToOutputStream(PrintWriter responseWriter, LogFile filteredLogFile) {
        for (LogLine logLine : filteredLogFile.getLines()) {
            logLine.print(responseWriter);
            responseWriter.println();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String[]> getParametersMap(HttpServletRequest req) {
        return req.getParameterMap();
    }

    LogFileFilterInteractor newInteractor() {
        return logFileFilterInteractorFactory.createInteractor();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        loadLodFileFilterFromConfig(config);
    }

    private void loadLodFileFilterFromConfig(ServletConfig config) {
        String configResource = config.getInitParameter(InitParameters.ConfigurationResource);
        InputStream configFile = getClass().getResourceAsStream("/"+ configResource);
        try {
            this.logFileFilterInteractorFactory = new XmlConfiguredLogFileFilterInteractorFactory(configFile);
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
