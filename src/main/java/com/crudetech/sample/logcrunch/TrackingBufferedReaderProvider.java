package com.crudetech.sample.logcrunch;


import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

abstract class TrackingBufferedReaderProvider implements TextFileLineIterable.BufferedReaderProvider {
    private final Set<BufferedReader> trackedReaders = new HashSet<BufferedReader>();

    @Override
    public BufferedReader newReader() {
        BufferedReader r = new BufferedReader(createNewReader()){
            @Override
            public void close() throws IOException {
                super.close();
                trackedReaders.remove(this);
            }
        };
        trackedReaders.add(r);
        return r;
    }

    abstract Reader createNewReader();

    @Override
    public void closeReader(BufferedReader reader) {
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            trackedReaders.remove(reader);
        }
    }

    @Override
    public boolean isClosed(BufferedReader reader) {
        return !trackedReaders.contains(reader);
    }

    void closeAllReaders() {
        for (Closeable trackedReader : trackedReaders) {
            close(trackedReader);
        }
        trackedReaders.clear();
    }

    private void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    boolean hasReaders() {
        return trackedReaders.isEmpty();
    }

    int amountOfReaders() {
        return trackedReaders.size();
    }
}
