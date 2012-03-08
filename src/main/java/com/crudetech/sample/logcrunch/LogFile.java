package com.crudetech.sample.logcrunch;

public interface LogFile {
    Iterable<? extends StringLogLine> getLines();
}
