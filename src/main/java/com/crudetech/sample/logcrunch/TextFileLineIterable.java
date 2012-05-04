package com.crudetech.sample.logcrunch;


import com.crudetech.sample.filter.CursorIterator;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

class TextFileLineIterable implements Iterable<String> {
    interface BufferedReaderProvider {
        BufferedReader newReader();

        void closeReader(BufferedReader reader);

        boolean isClosed(BufferedReader reader);
    }

    private final BufferedReaderProvider readerProvider;

    TextFileLineIterable(BufferedReaderProvider readerProvider) {
        this.readerProvider = readerProvider;
    }

    @Override
    public Iterator<String> iterator() {
        return new LineIterator();
    }

    private class LineIterator extends CursorIterator<String> {
        private final BufferedReader reader;
        private boolean closedReader = false;

        LineIterator() {
            this.reader = readerProvider.newReader();
            closeReaderOnEnd();
        }

        @Override
        protected Cursor<String> incrementCursor() {
            String nextLine = readNextLine();
            if (nextLine != null) {
                return Cursor.on(nextLine);
            }
            return Cursor.end();
        }

        private void closeReaderOnEnd() {
            if (!hasNext()) {
                closeReader();
            }
        }

        @Override
        public boolean hasNext() {
            return isBufferedReaderOpen()
                && super.hasNext();
        }

        private String readNextLine() {
            try {
                return reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String next() {
            try {
                return super.next();
            } finally {
                closeReaderOnEnd();
            }
        }

        @Override
        protected void verifyNextElement() {
            if (isBufferedReaderClosed() && !closedReader) {
                throw new ConcurrentModificationException();
            }
            super.verifyNextElement();
        }

        private void closeReader() {
            readerProvider.closeReader(reader);
            closedReader = true;
        }

        private boolean isBufferedReaderClosed() {
            return readerProvider.isClosed(reader);
        }
        private boolean isBufferedReaderOpen() {
            return !isBufferedReaderClosed();
        }
    }
}
