package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.ListLogFilesInteractor;
import com.crudetech.sample.logcrunch.LogFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class ListLogFilesServlet extends XmlConfiguredLogFileInteractorServlet<ListLogFilesInteractor>{
    // GET http://localhost:8080/logcrunch/list?logFileNamePattern=machinename101-%25d{yyyyMMdd}.log&searchRange=2007-05-06/2007-05-08
    @Override
    protected void doGet(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        ListLogFilesInteractor.Query query = buildQuery(getParametersMap(req), resp);
        if(resp.isCommitted()){
            return;
        }

        ListLogFilesInteractor listFiles = newInteractor();
        final PrintWriter responseWriter = resp.getWriter();
        listFiles.listFiles(query,new ListLogFilesInteractor.Result() {
            @Override
            public void listFile(LogFile logFile) {
                logFile.print(responseWriter);
                responseWriter.println();
            }

            @Override
            public void noFilesFound() {
                sendError(resp, HttpStatusCode.NotFound);
            }
        });

        if(resp.isCommitted()){
            return;
        }

        resp.setStatus(HttpStatusCode.Ok.Code);
    }

    private void sendError(HttpServletResponse resp, HttpStatusCode code) {
        try {
            resp.sendError(code.Code, code.Message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ListLogFilesInteractor.Query buildQuery(Map<String, String[]> parameterMap, HttpServletResponse resp) throws IOException {
        ListLogFilesInteractor.Query query = new ParameterListFilesQuery();
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
}
