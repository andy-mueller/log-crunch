package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.LogFileFilterInteractor;
import com.crudetech.sample.logcrunch.LogLevel;
import org.joda.time.Interval;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LogCrunchFilterServlet extends HttpServlet {
    class RequestParameters {
        static final String Level = "request";
        static final String SearchRange = "seachRange";
    }

    static enum HttpStatusCode {
        Ok(200, ""), BadFormat(404, "Bad format!");
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

        String[] levels = getRequestParameters(req, RequestParameters.Level);
        for (String level : levels) {
            query.levels.add(LogLevel.valueOf(level));
        }


        String[] searchIntervals = getRequestParameters(req, RequestParameters.SearchRange);
        if (searchIntervals.length == 0) {
            resp.sendError(LogCrunchFilterServlet.HttpStatusCode.BadFormat.Code, HttpStatusCode.BadFormat.Message);
            return;
        }
        try {
            for (String interval : searchIntervals) {
                query.searchIntervals.add(Interval.parse(interval));
            }
        } catch (IllegalArgumentException e) {
            resp.sendError(LogCrunchFilterServlet.HttpStatusCode.BadFormat.Code, HttpStatusCode.BadFormat.Message);
            return;
        }

        LogFileFilterInteractor logFileFilterInteractor = newInteractor();
        logFileFilterInteractor.getFilteredLogFiles(query);
    }

    private String[] getRequestParameters(HttpServletRequest req, String param) {
        String[] params = req.getParameterValues(param);
        return params != null ? params : new String[]{};
    }

    LogFileFilterInteractor newInteractor() {
        return null;
    }
}
