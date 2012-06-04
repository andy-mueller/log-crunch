package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.InteractorFactory;
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
import java.util.Map;

public abstract class XmlConfiguredLogFileInteractorServlet<TQuery, TResult, TInteractor> extends HttpServlet {
    InteractorFactory<TInteractor> filterLogFileInteractorFactory;

    @SuppressWarnings("unchecked")
    protected static Map<String, String[]> getParametersMap(HttpServletRequest req) {
        return req.getParameterMap();
    }


    @Override
    protected void doGet(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        TQuery query = buildQuery(getParametersMap(req), resp);
        if (resp.isCommitted()) {
            return;
        }

        LogFileInteractor<TQuery, TResult> interactor = newGenericInteractor();
        interactor.interact(query, createResult(resp));

        if (resp.isCommitted()) {
            return;
        }

        resp.setStatus(HttpStatusCode.Ok.Code);
    }

    protected abstract TResult createResult(HttpServletResponse resp);

    private TQuery buildQuery(Map<String, String[]> parameterMap, HttpServletResponse resp) throws IOException {
        TQuery query = createQuery();
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

    protected abstract TQuery createQuery();

    private void sendError(HttpServletResponse resp, HttpStatusCode code) {
        try {
            resp.sendError(code.Code, code.Message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected ParameterMapper buildParameterMapper(Map<String, String[]> parameterMap) {
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

    TInteractor newInteractor() {
        return filterLogFileInteractorFactory.createInteractor();
    }

    abstract LogFileInteractor<TQuery, TResult> newGenericInteractor();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        loadInteractorFromConfig(config);
    }

    private void loadInteractorFromConfig(ServletConfig config) {
        String configResource = config.getInitParameter(ServletInitParameters.ConfigurationResource);
        InputStream configFile = getClass().getResourceAsStream("/" + configResource);
        try {
            this.filterLogFileInteractorFactory = new XmlConfiguredInteractorFactory<TInteractor>(configFile);
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
