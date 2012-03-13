package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.MappingIterable;
import com.crudetech.sample.filter.UnaryFunction;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public abstract class BufferedReaderLogFile implements LogFile {
    public interface LogLineFactory {
        StringLogLine newLogLine(String lineContent);
    }

    private final LogLineFactory logLineFactory;
    private final Set<Closeable> trackedReaders = new HashSet<Closeable>();

    public BufferedReaderLogFile(LogLineFactory logLineFactory) {
        this.logLineFactory = logLineFactory;
    }

    @Override
    public Iterable<StringLogLine> getLines() {
        Iterable<String> textLines = new TextFileLineIterable(createNewReaderProvider());
        return new MappingIterable<String, StringLogLine>(textLines, selectLogLine());
    }

    private TextFileLineIterable.BufferedReaderProvider createNewReaderProvider() {
        return new TextFileLineIterable.BufferedReaderProvider() {
            @Override
            public BufferedReader newReader() {
                return createNewReader();
            }

            @Override
            public void closeReader(BufferedReader reader) {
                BufferedReaderLogFile.this.closeReader(reader);
            }
        };
    }

    private void closeReader(BufferedReader reader) {
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            trackedReaders.remove(reader);
        }
    }

    private UnaryFunction<String, StringLogLine> selectLogLine() {
        return new UnaryFunction<String, StringLogLine>() {
            @Override
            public StringLogLine evaluate(String argument) {
                return logLineFactory.newLogLine(argument);
            }
        };
    }

    protected abstract BufferedReader createNewReader();

    @Override
    public void close() {
    }
}
