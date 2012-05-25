package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.InteractorFactory;
import com.crudetech.sample.logcrunch.LogFileNamePattern;
import com.crudetech.sample.logcrunch.logback.LogbackLogFileNamePattern;
import org.joda.time.Interval;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class XmlConfiguredLogFileInteractorServlet<TInteractor> extends HttpServlet {
    InteractorFactory<TInteractor> filterLogFileInteractorFactory;

    @SuppressWarnings("unchecked")
    protected static Map<String, String[]> getParametersMap(HttpServletRequest req) {
        return req.getParameterMap();
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
