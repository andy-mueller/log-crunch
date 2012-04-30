package com.crudetech.sample.logcrunch;

import java.io.Reader;

public abstract class BufferedReaderLogFile implements LogFile {
    private final LogLineFactory logLineFactory;
    private final TrackingBufferedReaderProvider trackingBufferedReaderProvider;

    public interface LogLineFactory {
        Iterable<LogLine> logLines(Iterable<String> lines);

    }

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
        return logLineFactory.logLines(textLines);
    }

    protected abstract Reader createNewReader();

    @Override
    public void close() {
        trackingBufferedReaderProvider.closeAllReaders();
    }
}
