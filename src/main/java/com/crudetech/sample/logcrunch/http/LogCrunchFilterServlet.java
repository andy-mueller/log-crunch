package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.LogFileFilterInteractor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class LogCrunchFilterServlet extends HttpServlet {
    class RequestParameters {
        static final String Level = "level";
        static final String SearchRange = "searchRange";
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


    // GET http://localhost:8080/logcrunch/filter?fileName=machine01&searchRange=2007-05-07T13:55:22,100/2009-07-02&level=Info&level=Warn
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LogFileFilterInteractor.Query query = new LogFileFilterInteractor.Query();

        ParameterMapper mapper = new ParameterMapper(getParametersMap(req));

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
        logFileFilterInteractor.getFilteredLogFiles(query);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String[]> getParametersMap(HttpServletRequest req) {
        return req.getParameterMap();
    }

    LogFileFilterInteractor newInteractor() {
        return null;
    }
}
