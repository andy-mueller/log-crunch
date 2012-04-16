package com.crudetech.sample.logcrunch.logback;

import com.crudetech.sample.filter.MappingIterable;
import com.crudetech.sample.logcrunch.LogFileNamePattern;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.crudetech.sample.filter.Algorithm.accumulate;
import static com.crudetech.sample.filter.Strings.concat;
import static com.crudetech.sample.filter.Strings.regexQuote;
import static java.util.Arrays.asList;

public class LogbackLogFileNamePattern implements LogFileNamePattern {
    private static final Pattern datePattern = Pattern.compile("%d\\{.*\\}");
    private final String[] parts;
    private final DateTimeFormatter dateFormat;
    private final int dateFormatPatternLength;

    public LogbackLogFileNamePattern(String pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException();
        }
        parts = datePattern.split(pattern);
        String dateFormatPattern = extractDateFormat(pattern);
        dateFormatPatternLength = dateFormatPattern.length();
        dateFormat = DateTimeFormat.forPattern(dateFormatPattern);
    }

    @Override
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

    @Override
    public DateTime dateOf(String fileName) {
        int start = fileName.indexOf(parts[0]) + parts[0].length();
        int end = start + dateFormatPatternLength;
        String dateString = fileName.substring(start, end);

        return dateFormat.parseDateTime(dateString);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogbackLogFileNamePattern that = (LogbackLogFileNamePattern) o;

        return Arrays.equals(parts, that.parts);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(parts);
    }
}
