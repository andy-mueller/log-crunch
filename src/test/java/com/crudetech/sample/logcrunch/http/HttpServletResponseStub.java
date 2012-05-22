package com.crudetech.sample.logcrunch.http;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

import static org.mockito.Mockito.mock;

class HttpServletResponseStub extends HttpServletResponseWrapper{
    int sc;
    String msg;

    HttpServletResponseStub() {
        super(mock(HttpServletResponse.class));
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.sc = sc;
        this.msg = msg;
    }
}
