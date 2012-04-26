package com.crudetech.sample.logcrunch;

import java.util.List;

public class ArrayListLogFile implements LogFile {
    private final List<LogLine> lines;

    protected ArrayListLogFile(List<LogLine> lines) {
        this.lines = lines;
    }

    @Override
    public Iterable<LogLine> getLines() {
        return lines;
    }

    @Override
    public void close() {
    }
}
