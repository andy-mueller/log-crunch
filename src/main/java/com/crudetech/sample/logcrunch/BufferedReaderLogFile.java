package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.MappingIterable;
import com.crudetech.sample.filter.UnaryFunction;

import java.io.Reader;

public abstract class BufferedReaderLogFile implements LogFile {

    private final TrackingBufferedReaderProvider trackingBufferedReaderProvider;

    public interface LogLineFactory {
        StringLogLine newLogLine(String lineContent);
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
    public Iterable<StringLogLine> getLines() {
        Iterable<String> textLines = new TextFileLineIterable(trackingBufferedReaderProvider);
        return new MappingIterable<String, StringLogLine>(textLines, selectLogLine());
    }


    private UnaryFunction<StringLogLine, String> selectLogLine() {
        return new UnaryFunction<StringLogLine, String>() {
            @Override
            public StringLogLine evaluate(String argument) {
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
