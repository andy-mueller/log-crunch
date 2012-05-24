package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.InteractorFactory;
import com.crudetech.sample.logcrunch.ListLogFilesInteractor;
import com.crudetech.sample.logcrunch.LogFile;
import com.crudetech.sample.logcrunch.LogFileNamePattern;
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

public class ListLogFilesServlet extends HttpServlet{
    private InteractorFactory<ListLogFilesInteractor> listLogFilesInteractorFactory;

    // GET http://localhost:8080/logcrunch/list?logFileNamePattern=machinename101-%25d{yyyyMMdd}.log&searchRange=2007-05-06/2007-05-08
    @Override
    protected void doGet(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        ListLogFilesInteractor.Query query = buildQuery(getRequestParameters(req), resp);
        if(resp.isCommitted()){
            return;
        }

        ListLogFilesInteractor listFiles = createInteractor();
        final PrintWriter responseWriter = resp.getWriter();
        listFiles.listFiles(query,new ListLogFilesInteractor.Result() {
            @Override
            public void listFile(LogFile logFile) {
                logFile.print(responseWriter);
                responseWriter.println();
            }

            @Override
            public void noFilesFound() {
                sendError(resp, HttpStatusCode.NotFound);
            }
        });

        if(resp.isCommitted()){
            return;
        }

        resp.setStatus(HttpStatusCode.Ok.Code);
    }

    private void sendError(HttpServletResponse resp, HttpStatusCode code) {
        try {
            resp.sendError(code.Code, code.Message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String[]> getRequestParameters(HttpServletRequest req) {
        return req.getParameterMap();
    }

    private ListLogFilesInteractor.Query buildQuery(Map<String, String[]> parameterMap, HttpServletResponse resp) throws IOException {
        ListLogFilesInteractor.Query query = new ParameterListFilesQuery();
        ParameterMapper mapper = buildParameterMapper(parameterMap);
        try {
            mapper.mapTo(query);
        } catch (ParameterMapper.BadFormatException e) {
            sendError(resp, HttpStatusCode.BadFormat);
        } catch (ParameterMapper.NoParameterException e) {
            sendError(resp, HttpStatusCode.BadFormat);
        }
        return query;
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
    ListLogFilesInteractor createInteractor() {
        return listLogFilesInteractorFactory.createInteractor();
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
            this.listLogFilesInteractorFactory = new XmlConfiguredInteractorFactory<ListLogFilesInteractor>(configFile);
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
