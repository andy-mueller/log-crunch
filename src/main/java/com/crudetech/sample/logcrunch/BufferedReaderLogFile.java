package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.MappingIterable;
import com.crudetech.sample.filter.UnaryFunction;

import java.io.Reader;

public abstract class BufferedReaderLogFile implements LogFile {

    private final TrackingBufferedReaderProvider trackingBufferedReaderProvider;

    public interface LogLineFactory {
        LogLine newLogLine(String lineContent);
    }

    private final LogLineFactory logLineFactory;

    public BufferedReaderLogFile(LogLineFactory logLineFactory) {
        this.logLineFactory = logLineFactory;
        trackingBufferedReaderProvider = createNewReaderProvider();
    }

    private TrackingBufferedReaderProvider createNewReaderProvider() {
        return new TrackingBufferedReaderProvider() {
            @Override
            Reader createNewReader() {
                return BufferedReaderLogFile.this.createNewReader();
            }
        };
    }

    @Override
    public Iterable<LogLine> getLines() {
        Iterable<String> textLines = new TextFileLineIterable(trackingBufferedReaderProvider);
        return new MappingIterable<String, LogLine>(textLines, selectLogLine());
    }


    private UnaryFunction<LogLine, String> selectLogLine() {
        return new UnaryFunction<LogLine, String>() {
            @Override
            public LogLine evaluate(String argument) {
                return logLineFactory.newLogLine(argument);
            }
        };
    }

    protected abstract Reader createNewReader();

    @Override
    public void close() {
        trackingBufferedReaderProvider.closeAllReaders();
    }
}
