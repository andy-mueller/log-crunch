package com.crudetech.sample.logcrunch;


import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringLogLine implements LogLine {
    private final LogLevel level;
    private Date date;
    private final String line;
    private final SimpleDateFormat dateFormat;
    private final String logger;

    // %-4relative [%thread] %-5level %logger{35} - %msg %n

    public StringLogLine(String line, SimpleDateFormat dateFormat) {
        this.line = line;
        this.dateFormat = (SimpleDateFormat) dateFormat.clone();
        String[] token = line.split(" ");
        level = getLogLevel(token);

        date = getDate(token);
        logger = getLogger(token);
    }

    private String getLogger(String[] token) {
        return token[2];
    }

    private Date getDate(String[] token) {
        try {
            return dateFormat.parse(token[0] + " " + token[1]);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private LogLevel getLogLevel(String[] token) {
        String rawLevel = token[4].substring(0, token[4].length() - 1);
        if ("WARN".equals(rawLevel)) {
            return LogLevel.Warn;
        } else if ("INFO".equals(rawLevel)) {
            return LogLevel.Info;
        } else if ("DEBUG".equals(rawLevel)) {
            return LogLevel.Debug;
        } else {
            throw new IllegalArgumentException("Unknown loglevel " + rawLevel);
        }
    }

    private LogLevel getLogLevel() {
        return level;
    }

    private Date getDate() {
        return (Date) date.clone();
    }

    @Override
    public void print(PrintWriter writer) {
        writer.print(line);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringLogLine that = (StringLogLine) o;

        return !(line != null ? !line.equals(that.line) : that.line != null);
    }

    @Override
    public int hashCode() {
        return line != null ? line.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "StringLogLine{" +
                "line='" + line + '\'' +
                '}';
    }

    @Override
    public boolean hasDate(Date expected) {
        return getDate().equals(expected);
    }

    @Override
    public boolean hasLogLevel(LogLevel level) {
        return getLogLevel().equals(level);
    }

    @Override
    public boolean isInRange(DateTimeRange range) {
        return range.contains(getDate());
    }

    @Override
    public boolean hasLogger(Pattern loggerPattern) {
        Matcher matcher = loggerPattern.matcher(logger);
        return matcher.matches();
    }
}
