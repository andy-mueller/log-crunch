package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.FilterLogFileInteractor;
import com.crudetech.sample.logcrunch.LogLine;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class FilterLogFileServlet extends XmlConfiguredLogFileInteractorServlet<FilterLogFileInteractor.FilterQuery, FilterLogFileInteractor.FilterResult, FilterLogFileInteractor> {

    // GET http://localhost:8080/logcrunch/filter?logFileNamePattern=machinename101-%25d{yyyyMMdd}.log&searchRange=2007-05-06/2007-05-08&level=Info&level=Warn

    @Override
    protected FilterLogFileInteractor.FilterResult createResult(HttpServletResponse resp) {
        final HttpResponse response = new HttpResponse(resp);
        return new FilterLogFileInteractor.FilterResult() {
            @Override
            public void filteredLogLine(LogLine logLine) {
                response.writeLogLine(logLine);
            }

            @Override
            public void noFilesFound() {
                response.commitErrorResponse(HttpStatusCode.NotFound);
            }

            @Override
            public void noLinesFound() {
                response.commitResponse(HttpStatusCode.OkNoLinesFound);
            }
        };
    }

    @Override
    protected FilterLogFileInteractor.FilterQuery createQuery() {
        return new ParameterFilterQuery();
    }

    @Override
    LogFileInteractor<FilterLogFileInteractor.FilterQuery, FilterLogFileInteractor.FilterResult> newGenericInteractor() {
        return new LogFileInteractor<FilterLogFileInteractor.FilterQuery, FilterLogFileInteractor.FilterResult>() {
            private final FilterLogFileInteractor realInteractor = newInteractor();
            @Override
            public void interact(FilterLogFileInteractor.FilterQuery q, FilterLogFileInteractor.FilterResult r) {
                realInteractor.getFilteredLines(q, r);
            }
        };
    }

    private class HttpResponse {
        private final HttpServletResponse servletResponse;

        HttpResponse(HttpServletResponse servletResponse) {
            this.servletResponse = servletResponse;
        }

        void commitResponse(HttpStatusCode status) {
            getWriter().print(status.Message);
            servletResponse.setStatus(status.Code);
        }

        private PrintWriter getWriter() {
            try {
                return servletResponse.getWriter();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        void commitErrorResponse(HttpStatusCode code) {
            try {
                servletResponse.sendError(code.Code, code.Message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        void writeLogLine(LogLine logLine) {
            PrintWriter responseWriter = getWriter();
            logLine.print(responseWriter);
            responseWriter.println();
        }
    }
}
