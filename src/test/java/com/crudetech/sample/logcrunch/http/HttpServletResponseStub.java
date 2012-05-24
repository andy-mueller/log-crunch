package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.filter.Strings;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import static org.mockito.Mockito.mock;

class HttpServletResponseStub extends HttpServletResponseWrapper {
    int sc;
    String content = "";

    HttpServletResponseStub() {
        super(mock(HttpServletResponse.class));
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.sc = sc;
        this.content += msg;
    }

    @Override
    public void setStatus(int sc) {
        this.sc = sc;
    }

    @Override
    public boolean isCommitted() {
        return sc != 0;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(mock(OutputStream.class)) {
            @Override
            public void print(String s) {
                content += s;
            }

            @Override
            public void println() {
                content += Strings.lineSeparator();
            }
        };
    }
}
