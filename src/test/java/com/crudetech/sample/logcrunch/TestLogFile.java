package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.MappingIterable;
import com.crudetech.sample.filter.UnaryFunction;
import com.crudetech.sample.logcrunch.logback.LogbackLogFileNamePattern;
import com.crudetech.sample.logcrunch.logback.LogbackLogLine;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.rules.ExternalResource;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;


public abstract class TestLogFile extends ExternalResource implements LogFile {
    public static final Charset Encoding = Charset.forName("UTF-8");
    public static String DateFormatPattern = "yyyy-MM-dd HH:mm:ss";
    static DateTimeFormatter DateFormat = DateTimeFormat.forPattern(DateFormatPattern);
    static DateTime SampleInfoLineDate = new DateTime(2009, 6, 7, 13,23, 57);
    static final LogLine SampleInfoLine = LogbackBridge.createLogLine(MessageFormat.format("{0} demo.ZeroToFour main INFO: This is an informative message", DateFormat.print(SampleInfoLineDate)), DateFormat);

    final String name;
    List<LogLine> logLines;
    DateTime baseLine;

    static class LogbackBridge{
        static LogLine createLogLine(String rawLine, DateTimeFormatter dateFormat){
           return new LogbackLogLine(rawLine, dateFormat);
        }
        static LogFileNamePattern createFileNamePattern(String pattern){
            return new LogbackLogFileNamePattern(pattern);
        }
    }


    public TestLogFile(String name) {
        this.name = name;
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        baseLine = DateTime.now().minusMinutes(1);
        DateTime line2TimeStamp = baseLine.plusSeconds(1);
        String line2 = generateUniqueLogLine(line2TimeStamp, "ERROR");
        DateTime line3TimeStamp = baseLine.plusSeconds(2);
        String line3 = generateUniqueLogLine(line3TimeStamp, "WARN");
        DateTime line4TimeStamp = baseLine.plusSeconds(2);
        String line4 = generateUniqueLogLine(line4TimeStamp, "INFO");

        StringWriter sampleLine = new StringWriter();
        SampleInfoLine.print(new PrintWriter(sampleLine));
        logLines = Collections.unmodifiableList(asList(
                SampleInfoLine,
                LogbackBridge.createLogLine(line2, DateFormat),
                LogbackBridge.createLogLine(line3, DateFormat),
                LogbackBridge.createLogLine(line4, DateFormat)
        ));
    }

    private String generateUniqueLogLine(DateTime timeStamp, String level) {
        return MessageFormat.format("{0} {1} subroutine {3}: {2}", DateFormat.print(timeStamp), getClass().getName(), UUID.randomUUID(), level);
    }

    @Override
    public Iterable<LogLine> getLines() {
        return logLines;
    }

    public Iterable<String> getLinesAsString() {
        return new MappingIterable<LogLine, String>(getLines(), lineToString());
    }

    private UnaryFunction<String, LogLine> lineToString() {
        return new UnaryFunction<String, LogLine>() {
            @Override
            public String evaluate(LogLine argument) {
                StringWriter sw = new StringWriter();
                argument.print(new PrintWriter(sw));
                return sw.toString();
            }
        };
    }

    @Override
    public void close() {
    }

    public static BufferedReaderLogFile.LogLineFactory logLineFactory(){
        return new BufferedReaderLogFile.LogLineFactory() {
            @Override
            public Iterable<LogLine> logLines(Iterable<String> lines) {
                return new MappingIterable<String, LogLine>(lines, createSingleLine());
            }

            private UnaryFunction<LogLine, String> createSingleLine() {
                return new UnaryFunction<LogLine, String>() {
                    @Override
                    public LogLine evaluate(String lineContent) {
                        return LogbackBridge.createLogLine(lineContent, DateFormat);
                    }
                };
            }
        };
    }
}
