package com.crudetech.sample.logcrunch.http;

enum HttpStatusCode {
    Ok(200, ""),
    OkNoLinesFound(200, "No lines found that match the criteria"),
    BadFormat(400, "Bad format!"),
    NotFound(404, "No files found!");
    final String Message;
    final int Code;

    HttpStatusCode(int code, String message) {
        this.Code = code;
        Message = message;
    }
}
