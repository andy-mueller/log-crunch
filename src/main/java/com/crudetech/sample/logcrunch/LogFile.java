package com.crudetech.sample.logcrunch;

import java.io.PrintWriter;

public interface LogFile {
    Iterable<LogLine> getLines();

    void close();

    void print(PrintWriter w);
}
