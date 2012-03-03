package com.crudetech.sample.logcrunch;

import java.io.IOException;
import java.io.InputStream;

public class LogFile {
    private final InputStream logFileStream;

    public LogFile(InputStream logFile) {

        logFileStream = logFile;
    }

    public void close() {
        try {
            logFileStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
