package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.ListLogFilesInteractor;
import com.crudetech.sample.logcrunch.LogFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ListLogFilesServlet extends XmlConfiguredLogFileInteractorServlet<ListLogFilesInteractor.Query, ListLogFilesInteractor.Result, ListLogFilesInteractor> {
    // GET http://localhost:8080/logcrunch/list?logFileNamePattern=machinename101-%25d{yyyyMMdd}.log&searchRange=2007-05-06/2007-05-08

    @Override
    protected ListLogFilesInteractor.Result createResult(final HttpServletResponse resp) {
        final PrintWriter responseWriter = getWriter(resp);

        return new ListLogFilesInteractor.Result() {
            @Override
            public void listFile(LogFile logFile) {
                logFile.print(responseWriter);
                responseWriter.println();
            }

            @Override
            public void noFilesFound() {
                sendError(resp, HttpStatusCode.NotFound);
            }
        };
    }

    private PrintWriter getWriter(HttpServletResponse resp) {
        try {
            return resp.getWriter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendError(HttpServletResponse resp, HttpStatusCode code) {
        try {
            resp.sendError(code.Code, code.Message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    LogFileInteractor<ListLogFilesInteractor.Query, ListLogFilesInteractor.Result> newGenericInteractor() {
        return new LogFileInteractor<ListLogFilesInteractor.Query, ListLogFilesInteractor.Result>() {
            ListLogFilesInteractor realInteractor = newInteractor();

            @Override
            public void interact(ListLogFilesInteractor.Query q, ListLogFilesInteractor.Result r) {
                realInteractor.listFiles(q, r);
            }
        };
    }

    @Override
    protected ListLogFilesInteractor.Query createQuery() {
        return new ParameterListFilesQuery();
    }
}
