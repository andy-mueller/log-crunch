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
    class RequestParameters{
        static final String Level = "request";
        static final String SearchRange = "seachRange";
    }

    // GET http://localhost:8080/logcrunch/filter?fileName=machine01&searchRange=[20070507,20090702)&level=Info&level=Warn
    // GET http://localhost:8080/logcrunch/filter?fileName=machine01&searchRange=20070507/20090702&level=Info&level=Warn
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LogFileFilterInteractor.Query query = new LogFileFilterInteractor.Query();

        String[] levels = getRequestParameters(req, RequestParameters.Level);
        for (String level : levels) {
            query.levels.add(LogLevel.valueOf(level));
        }
        
        String[] searchIntervals = getRequestParameters(req, RequestParameters.SearchRange);
        if(searchIntervals.length == 0){
            resp.sendError(404, "There must be at least one search interval specified!");
            return;
        }
        for (String interval : searchIntervals) {
            query.searchIntervals.add(Interval.parse(interval));
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
