package com.crudetech.sample.logcrunch.http;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LogCrunchServlet extends HttpServlet{

    // GET http://xyz.com/context/LogCrunch/?name=machine01&from=20070507&to=20090702
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }
}
