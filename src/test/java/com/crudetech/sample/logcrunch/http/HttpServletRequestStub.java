package com.crudetech.sample.logcrunch.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

class HttpServletRequestStub extends HttpServletRequestWrapper {
    private final Map<String, String[]> parameters = new HashMap<String, String[]>();

    HttpServletRequestStub() {
        super(mock(HttpServletRequest.class));
    }

    void putParameter(String name, String... value) {
        parameters.put(name, value);
    }

    @Override
    public Map getParameterMap() {
        return parameters;
    }
}
