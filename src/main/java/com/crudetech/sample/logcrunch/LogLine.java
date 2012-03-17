package com.crudetech.sample.logcrunch;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.io.PrintWriter;
import java.util.regex.Pattern;

public interface LogLine {
    void print(PrintWriter writer);

    boolean hasDate(DateTime expected);

    boolean hasLogLevel(LogLevel level);

    boolean isInRange(Interval range);

    boolean hasLogger(Pattern loggerPattern);
}
