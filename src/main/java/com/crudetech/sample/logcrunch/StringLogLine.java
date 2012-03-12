package com.crudetech.sample.logcrunch;


import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringLogLine {
    private final LogLevel level;
    private Date date;
    private final String line;
    private final SimpleDateFormat dateFormat;

    public StringLogLine(String line, SimpleDateFormat dateFormat) {
        this.line = line;
        this.dateFormat = (SimpleDateFormat) dateFormat.clone();
        String[] token = line.split(" ");
        level = getLogLevel(token);

        date = getDate(token);
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

    public boolean hasDate(Date expected) {
        return getDate().equals(expected);
    }

    public boolean hasLogLevel(LogLevel level) {
        return getLogLevel().equals(level);
    }
}
