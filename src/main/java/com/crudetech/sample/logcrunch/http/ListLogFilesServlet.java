package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.ListLogFilesInteractor;
import com.crudetech.sample.logcrunch.LogFile;
import com.crudetech.sample.logcrunch.LogFileNamePattern;
import com.crudetech.sample.logcrunch.logback.LogbackLogFileNamePattern;
import org.joda.time.Interval;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class ListLogFilesServlet extends HttpServlet{
    // GET http://localhost:8080/logcrunch/list?logFileNamePattern=machinename101-%25d{yyyyMMdd}.log&searchRange=2007-05-06/2007-05-08
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ListLogFilesInteractor.Query query = buildQuery(getRequestParameters(req), resp);
        ListLogFilesInteractor listFiles = createInteractor();
        listFiles.listFiles(query,new ListLogFilesInteractor.Result() {
            @Override
            public void listFile(LogFile logFile) {
                throw new UnsupportedOperationException("Implement me!");
            }
        });
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
            resp.sendError(HttpStatusCode.BadFormat.Code, HttpStatusCode.BadFormat.Message);
        } catch (ParameterMapper.NoParameterException e) {
            resp.sendError(HttpStatusCode.BadFormat.Code, HttpStatusCode.BadFormat.Message);
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
        throw new UnsupportedOperationException("Implement me!");
    }
}
