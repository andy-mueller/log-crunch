package com.crudetech.sample.logcrunch;

public interface LogFile {
    Iterable<LogLine> getLines();

    void close();
}
