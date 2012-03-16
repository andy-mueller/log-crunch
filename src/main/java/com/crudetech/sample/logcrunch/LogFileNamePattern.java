package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.MappingIterable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.crudetech.sample.filter.Algorithm.accumulate;
import static com.crudetech.sample.filter.Strings.concat;
import static com.crudetech.sample.filter.Strings.regexQuote;
import static java.util.Arrays.asList;

public class LogFileNamePattern {
    private static final Pattern datePattern = Pattern.compile("%d\\{.*\\}");
    private final String[] parts;
    private final SimpleDateFormat dateFormat;

    public LogFileNamePattern(String pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException();
        }
        parts = datePattern.split(pattern);
        dateFormat = extractDateFormat(pattern);
    }

    public boolean matches(String fileName) {
        Iterable<String> quotedParts = new MappingIterable<String, String>(asList(parts), regexQuote());
        String comparePattern = accumulate(".*", quotedParts, concat());
        return Pattern.matches(comparePattern, fileName);
    }


    private static SimpleDateFormat extractDateFormat(String patternString) {
        Matcher dateMatcher = datePattern.matcher(patternString);
        if (!dateMatcher.find()) {
            throw new IllegalArgumentException();
        }
        int start = dateMatcher.start();
        int end = dateMatcher.end();
        String dateVal = patternString.substring(start + 3, end - 1);
        return new SimpleDateFormat(dateVal);

    }

    public Date dateOf(String fileName) {
        int start = fileName.indexOf(parts[0]) + parts[0].length();
        int end = start + dateFormat.toPattern().length();
        String dateString = fileName.substring(start, end);

        return parseDate(dateString);
    }

    private Date parseDate(String dateString) {
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
