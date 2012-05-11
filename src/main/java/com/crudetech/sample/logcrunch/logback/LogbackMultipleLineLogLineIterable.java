package com.crudetech.sample.logcrunch.logback;

import com.crudetech.sample.filter.CursorIterator;
import com.crudetech.sample.logcrunch.LogLine;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Iterator;

public class LogbackMultipleLineLogLineIterable implements Iterable<LogLine>{
    private final Iterable<String> rawTextLines;
    private final DateTimeFormatter formatter;

    public LogbackMultipleLineLogLineIterable(Iterable<String> rawTextLines, String dateFormat) {
        this.rawTextLines = rawTextLines;
        this.formatter = DateTimeFormat.forPattern(dateFormat);
    }

    @Override
    public Iterator<LogLine> iterator() {
        return new CursorIterator<LogLine>() {
            private final Iterator<String> rawLines = rawTextLines.iterator();

            @Override
            protected Cursor<LogLine> incrementCursor() {
                if(rawLines.hasNext()){
                    LogLine currentLogLine = new LogbackLogLine(rawLines.next(), formatter);
                    return Cursor.on(currentLogLine);
                }
                return Cursor.end();
            }
        };
    }
}
