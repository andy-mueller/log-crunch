package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.MappingIterable;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.crudetech.sample.filter.Algorithm.accumulate;
import static com.crudetech.sample.filter.Strings.concat;
import static com.crudetech.sample.filter.Strings.regexQuote;
import static java.util.Arrays.asList;

public class LogFileNamePattern {
    private static final Pattern datePattern = Pattern.compile("%d\\{.*\\}");
    private final String[] parts;
    private final DateTimeFormatter dateFormat;
    private final int dateFormatPatternLength;

    public LogFileNamePattern(String pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException();
        }
        parts = datePattern.split(pattern);
        String dateFormatPattern = extractDateFormat(pattern);
        dateFormatPatternLength = dateFormatPattern.length();
        dateFormat = DateTimeFormat.forPattern(dateFormatPattern);
    }

    public boolean matches(String fileName) {
        Iterable<String> quotedParts = new MappingIterable<String, String>(asList(parts), regexQuote());
        String comparePattern = accumulate(".*", quotedParts, concat());
        return Pattern.matches(comparePattern, fileName);
    }


    private static String extractDateFormat(String fileNamePattern) {
        Matcher dateMatcher = datePattern.matcher(fileNamePattern);
        if (!dateMatcher.find()) {
            throw new IllegalArgumentException();
        }
        int start = dateMatcher.start();
        int end = dateMatcher.end();
        return fileNamePattern.substring(start + 3, end - 1);
    }

    public DateTime dateOf(String fileName) {
        int start = fileName.indexOf(parts[0]) + parts[0].length();
        int end = start + dateFormatPatternLength;
        String dateString = fileName.substring(start, end);

        return dateFormat.parseDateTime(dateString);
    }
}
