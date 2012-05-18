package com.crudetech.sample.logcrunch;

import java.util.Collections;
import java.util.List;

public class ArrayListLogFile implements LogFile {
    private final List<LogLine> lines;
    private boolean isClosed  = false;

    protected ArrayListLogFile(List<LogLine> lines) {
        this.lines = lines;
    }

    public ArrayListLogFile() {
        this(Collections.<LogLine>emptyList());
    }

    @Override
    public Iterable<LogLine> getLines() {
        return lines;
    }

    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void close() {
        isClosed = true;
    }
}
