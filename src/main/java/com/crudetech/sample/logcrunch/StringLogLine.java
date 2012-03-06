package com.crudetech.sample.logcrunch;


import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringLogLine {
    private final String level;
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

    private String getLogLevel(String[] token) {
        return token[4].substring(0, token[4].length()-1);
    }

    public String getLogLevel() {
        return level;
    }

    public Date getDate() {
        return (Date) date.clone();
    }

    public void print(PrintWriter writer) {
         writer.print(line);
    }
}
