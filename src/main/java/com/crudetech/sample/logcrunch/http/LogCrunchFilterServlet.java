package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.LogFileFilterInteractor;
import com.crudetech.sample.logcrunch.LogLevel;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LogCrunchFilterServlet extends HttpServlet {

    // GET http://xyz.com/context/LogCrunch/?fileName=machine01&from=20070507&to=20090702&level=Info&level=Warn
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LogFileFilterInteractor.Query query = new LogFileFilterInteractor.Query();
        String[] levels = req.getParameterValues("level");
        for (String level : levels) {
            query.levels.add(LogLevel.valueOf(level));
        }

        LogFileFilterInteractor logFileFilterInteractor = newInteractor();
        logFileFilterInteractor.getFilteredLogFiles(query);
    }

    LogFileFilterInteractor newInteractor() {
        return null;
    }
}
