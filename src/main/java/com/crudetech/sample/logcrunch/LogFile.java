package com.crudetech.sample.logcrunch;

public interface LogFile {
    Iterable<StringLogLine> getLines();

    void close();
}
