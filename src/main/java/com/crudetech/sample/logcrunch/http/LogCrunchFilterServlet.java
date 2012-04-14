package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.LogFile;
import com.crudetech.sample.logcrunch.LogFileFilterInteractor;
import com.crudetech.sample.logcrunch.LogFileFilterInteractorFactory;
import com.crudetech.sample.logcrunch.LogLine;
import com.crudetech.sample.logcrunch.ParameterMapper;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Map;

public class LogCrunchFilterServlet extends HttpServlet {
    LogFileFilterInteractorFactory logFileFilterInteractorFactory;

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

    static class InitParameters {
        static final String SearchPath = "searchPath";
        static final String Encoding = "encoding";
        static final String LogLineFormat = "logLineFormat";
    }


    // GET http://localhost:8080/logcrunch/filter?fileName=machine01-%d{yyyMMdd}.log&searchRange=2007-05-07T13:55:22,100/2009-07-02&level=Info&level=Warn
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
        Iterable<LogFile> filteredLogFiles = logFileFilterInteractor.getFilteredLogFiles(query);
        writeFilteredLogFilesToOutputStream(resp.getWriter(), filteredLogFiles);
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
        File searchPath = new File(config.getInitParameter(InitParameters.SearchPath));
        Charset encoding = Charset.forName(config.getInitParameter(InitParameters.Encoding));
        String logLineFormat = config.getInitParameter(InitParameters.LogLineFormat);

        this.logFileFilterInteractorFactory = new LogbackLogFileFilterInteractorFactory(searchPath, encoding, logLineFormat);
    }
}
