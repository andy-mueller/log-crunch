package com.crudetech.sample.logcrunch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogFileNamePattern {
    private final String pattern;    //logFile.%d{yyyy-MM-dd}.log
    private static final Pattern datePattern = Pattern.compile("%d\\{.*\\}");

    public LogFileNamePattern(String pattern) {
        this.pattern = pattern;
    }

    public boolean matches(String fileName) {
        String[] parts = datePattern.split(pattern);
        String comparePattern = ".*";
        for (String part : parts) {
            comparePattern += Pattern.quote(part) + ".*";
        }

        boolean matches = Pattern.matches(comparePattern, fileName);

        return matches;
    }

    private SimpleDateFormat extractDateFormat() {
        Matcher dateMatcher = datePattern.matcher(pattern);

        if (dateMatcher.find()) {
            int start = dateMatcher.start();
            int end = dateMatcher.end();
            String dateVal = pattern.substring(start + 3, end - 1);
            return new SimpleDateFormat(dateVal);
        }
        throw new IllegalArgumentException();
    }

    public Date dateOf(String fileName) {
        SimpleDateFormat frm = extractDateFormat();
        String[] parts = datePattern.split(pattern);
        
        int start = fileName.indexOf(parts[0]) + parts[0].length();
        int end = start + frm.toPattern().length();
        
        String dateString = fileName.substring(start, end);

        try {
            return frm.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
