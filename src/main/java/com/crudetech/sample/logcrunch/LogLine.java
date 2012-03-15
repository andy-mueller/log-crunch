package com.crudetech.sample.logcrunch;

import java.io.PrintWriter;
import java.util.Date;
import java.util.regex.Pattern;

public interface LogLine {
    void print(PrintWriter writer);

    boolean hasDate(Date expected);

    boolean hasLogLevel(LogLevel level);

    boolean isInRange(DateTimeRange range);

    boolean hasLogger(Pattern loggerPattern);
}
